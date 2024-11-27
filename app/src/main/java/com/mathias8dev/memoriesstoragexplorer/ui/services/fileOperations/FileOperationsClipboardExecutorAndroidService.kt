package com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.util.fastFilter
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.data.event.Event
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FastFileCopyController
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FastFileCopyListener
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FileExistsAction
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations.FileOperationsService
import com.mathias8dev.memoriesstoragexplorer.domain.utils.CoroutineScopeOwner
import com.mathias8dev.memoriesstoragexplorer.domain.utils.CoroutineScopeProvider
import com.mathias8dev.memoriesstoragexplorer.domain.utils.removeIf
import com.mathias8dev.memoriesstoragexplorer.domain.utils.renameFileIfExists
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntry
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntryPayload
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asContentSize
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asSelectedPathView
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.minutes


interface FileOperationsClipboardExecutorAndroidService : FileOperationsAndroidService, ClipboardExecutor

class FileOperationsClipboardExecutorAndroidServiceImpl : Service(), FileOperationsClipboardExecutorAndroidService, KoinComponent, CoroutineScopeProvider by CoroutineScopeOwner() {

    private val fileOperationsService: FileOperationsService by inject()

    private val binder: IBinder by lazy { FileOperationsBinder() }
    private val jobsEntries by lazy { ConcurrentHashMap<String, Job>() }
    private val _fileOperationsProgress by lazy { mutableStateMapOf<String, FileOperationProgress>() }
    override val fileOperationsProgress: Map<String, FileOperationProgress> = _fileOperationsProgress

    private val _skippedOperations by lazy { mutableStateListOf<SkippedOperation>() }
    override val skippedOperations: List<SkippedOperation>
        get() = _skippedOperations

    private val fileOperationsMutex = Mutex()

    private val _events = MutableSharedFlow<Event>(replay = 5)

    private var jobsEntriesCleanJob: Job? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        runJobsCleaner()
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        super.onDestroy()
        jobsEntriesCleanJob?.cancel()
        jobsEntries.forEach { (_, job) ->
            job.cancel()
        }
        _fileOperationsProgress.clear()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun runJobsCleaner() {
        jobsEntriesCleanJob = coroutineScope.launch {
            while (true) {
                delay(10.minutes)
                jobsEntries.removeIf { _, job -> job.isCompleted || job.isCancelled }
            }
        }
    }

    inner class FileOperationsBinder : Binder() {
        val service: FileOperationsClipboardExecutorAndroidService
            get() = this@FileOperationsClipboardExecutorAndroidServiceImpl
    }

    override fun copyFile(sourceFilePath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction, uid: String) {
        Timber.d("OnCopyFile: $sourceFilePath to $destinationDirectoryPath with action $fileExistsAction")
        val job = coroutineScope.launch {
            copyFileSync(sourceFilePath, destinationDirectoryPath, fileExistsAction, uid)
        }

        jobsEntries[uid] = job
    }

    override suspend fun copyFileSync(sourceFilePath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction, uid: String): Boolean {
        return processFileSync(sourceFilePath, destinationDirectoryPath, fileExistsAction, uid, FileOperation.COPY_FILE)
    }

    override fun moveFile(sourceFilePath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction, uid: String) {
        val job = coroutineScope.launch {
            moveFileSync(sourceFilePath, destinationDirectoryPath, fileExistsAction, uid)
        }

        jobsEntries[uid] = job
    }


    override suspend fun moveFileSync(sourceFilePath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction, uid: String): Boolean {
        return processFileSync(sourceFilePath, destinationDirectoryPath, fileExistsAction, uid, FileOperation.MOVE_FILE)
    }

    private suspend fun processFileSync(
        sourceFilePath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction,
        uid: String,
        operation: FileOperation
    ): Boolean {
        val sourceFile = File(sourceFilePath)
        val destinationFile = File(destinationDirectoryPath, sourceFile.name)
        val controller = FastFileCopyController()
        return kotlin.runCatching {
            fileOperationsService.fastCopyFile(
                sourceFile,
                destinationFile,
                fileExistsAction,
                object : FastFileCopyListener {
                    val mutex = Mutex()
                    var totalBytesCopied = 0L
                    var copyRate = 0L
                    var lastOperationTime = System.currentTimeMillis()
                    var lastCopiedBytes = 0L

                    override suspend fun onProgressUpdate(bytesCopied: Long, totalFileSize: Long, sourceFilePath: String?, destinationFilePath: String?) {

                        mutex.withLock {
                            totalBytesCopied += bytesCopied
                            lastCopiedBytes += bytesCopied
                            val progress = totalBytesCopied.toFloat() / totalFileSize.toFloat()

                            _events.emit(
                                FileOperationsAndroidService.FileOperationsEvent.ProgressEvent(
                                    progress = progress.times(100).toInt(),
                                    uid = uid,
                                    operation = operation
                                )
                            )

                            val now = System.currentTimeMillis()
                            if (now - lastOperationTime >= 1000) {
                                copyRate = lastCopiedBytes
                                lastCopiedBytes = 0L
                                lastOperationTime = now
                            }

                            _fileOperationsProgress[uid] = FileOperationProgress(
                                sourceFilePath = sourceFile.absolutePath,
                                destinationFilePath = destinationFile.absolutePath,
                                progress = progress,
                                totalBytesCopied = totalBytesCopied,
                                copyRate = copyRate,
                                operation = operation,
                                controller = controller
                            )
                        }

                    }

                    override suspend fun onFileExists(sourceFilePath: String?, destinationFilePath: String?) {
                        fileOperationsMutex.withLock {
                            _fileOperationsProgress.remove(uid)
                        }
                        _skippedOperations.add(
                            SkippedOperation(
                                uid = uid,
                                sourceFilePath = sourceFile.absolutePath,
                                destinationFilePath = destinationFile.absolutePath,
                                intent = operation
                            )
                        )
                        jobsEntries.remove(uid)
                    }

                    override suspend fun onOperationDone(totalFileSize: Long, timeTaken: Long, sourceFilePath: String?, destinationFilePath: String?) {
                        Timber.d("File copied successfully")
                        Timber.d("FileOperationsProgress: $_fileOperationsProgress")
                        Timber.d("The uid is $uid")
                        Timber.d("Remove $uid from fileOperationsProgress")
                        _fileOperationsProgress.remove(uid)
                        Timber.d("FileOperationsProgress: $_fileOperationsProgress")
                        _events.emit(
                            FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                                status = FileOperationsAndroidService.Status.SUCCESS,
                                uid = uid,
                                operation = operation
                            )
                        )
                        stopByUids(uid)
                    }

                    override suspend fun onOperationFailed(throwable: Throwable, sourceFilePath: String?, destinationFilePath: String?) {
                        throw throwable
                    }

                    override suspend fun onOperationNotPermitted(sourceFilePath: String?, destinationFilePath: String?) {
                        super.onOperationNotPermitted(sourceFilePath, destinationFilePath)
                        _events.emit(
                            FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                                status = FileOperationsAndroidService.Status.NOT_PERMITTED,
                                uid = uid,
                                operation = operation
                            )
                        )
                    }
                },
                controller
            )
        }.onFailure {
            fileOperationsMutex.withLock {
                _fileOperationsProgress.remove(uid)
            }
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.ERROR,
                    uid = uid,
                    operation = operation
                )
            )
            jobsEntries.remove(uid)
        }.onSuccess {
            if (it && operation == FileOperation.MOVE_FILE) {
                fileOperationsService.deleteFile(sourceFilePath)
            }
        }.getOrDefault(false)
    }


    override fun silentDeleteFile(filePath: String, uid: String) {
        val job = coroutineScope.launch {
            silentDeleteFileSync(filePath, uid)
        }

        jobsEntries[uid] = job
    }

    override suspend fun silentDeleteFileSync(filePath: String, uid: String) {
        kotlin.runCatching {
            fileOperationsService.deleteFile(filePath)
        }.onFailure {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.ERROR,
                    uid = uid,
                    operation = FileOperation.DELETE_FILE
                )
            )
            jobsEntries.remove(uid)
        }.onSuccess {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.SUCCESS,
                    uid = uid,
                    operation = FileOperation.DELETE_FILE
                )
            )
            jobsEntries.remove(uid)
        }
    }

    override fun renameFile(filePath: String, newFileName: String, uid: String) {
        val job = coroutineScope.launch {
            renameFileSync(filePath, newFileName, uid)
        }

        jobsEntries[uid] = job
    }

    override suspend fun renameFileSync(filePath: String, newFileName: String, uid: String) {
        kotlin.runCatching {
            fileOperationsService.renameFile(filePath, newFileName)
        }.onFailure {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.ERROR,
                    uid = uid,
                    operation = FileOperation.RENAME_FILE
                )
            )
            jobsEntries.remove(uid)
        }.onSuccess {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.SUCCESS,
                    uid = uid,
                    operation = FileOperation.RENAME_FILE
                )
            )
            jobsEntries.remove(uid)
        }
    }

    override fun copyDirectory(directoryPath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction, uid: String) {
        val job = coroutineScope.launch {
            copyDirectorySync(directoryPath, destinationDirectoryPath, fileExistsAction, uid)
        }

        jobsEntries[uid] = job
    }

    // Copy only the content of the directory
    override suspend fun copyDirectorySync(directoryPath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction, uid: String): Boolean {
        return processDirectoryOperation(
            directoryPath,
            destinationDirectoryPath,
            fileExistsAction,
            uid,
            FileOperation.COPY_DIRECTORY,
        )
    }

    override fun moveDirectory(directoryPath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction, uid: String) {
        val job = coroutineScope.launch {
            moveDirectorySync(directoryPath, destinationDirectoryPath, fileExistsAction, uid)
        }

        jobsEntries[uid] = job
    }

    // Move only the content of the directory
    override suspend fun moveDirectorySync(directoryPath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction, uid: String): Boolean {
        return processDirectoryOperation(
            directoryPath,
            destinationDirectoryPath,
            fileExistsAction,
            uid,
            FileOperation.MOVE_DIRECTORY,
        )
    }


    private suspend fun processDirectoryOperation(
        directoryPath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction,
        uid: String,
        operation: FileOperation,
    ): Boolean = coroutineScope {
        val directoryFile = File(directoryPath)
        val destinationDirectoryFile = File(destinationDirectoryPath)
        Timber.d("onProcessDirectoryOperation ($operation): $directoryPath to ${destinationDirectoryFile.path} with action $fileExistsAction")


        val controller = FastFileCopyController()
        val fileSize = async { directoryFile.asContentSize() }
        val pathView = async { directoryFile.asSelectedPathView(true) }
        kotlin.runCatching {
            fileOperationsService.fastCopyDirectory(
                directoryFile,
                destinationDirectoryFile,
                fileExistsAction,
                object : FastFileCopyListener {
                    val mutex = Mutex()
                    var totalBytesCopied = 0L
                    var copyRatePerSecond = 0L
                    var bytesCopiedSinceLastSecond = 0L
                    var previousCall = System.currentTimeMillis()

                    override suspend fun onProgressUpdate(bytesCopied: Long, totalFileSize: Long, sourceFilePath: String?, destinationFilePath: String?) {
                        coroutineScope.launch {
                            val now = System.currentTimeMillis()
                            mutex.withLock {
                                totalBytesCopied += bytesCopied
                                bytesCopiedSinceLastSecond += bytesCopied
                                val progress = totalBytesCopied.toFloat() / fileSize.await()
                                if (now - previousCall >= 1000) {
                                    copyRatePerSecond = bytesCopiedSinceLastSecond
                                    previousCall = now
                                    bytesCopiedSinceLastSecond = 0
                                }

                                _fileOperationsProgress[uid] = FileOperationProgress(
                                    sourceFilePath = directoryFile.absolutePath,
                                    destinationFilePath = destinationDirectoryFile.absolutePath,
                                    progress = progress,
                                    controller = controller,
                                    isDirectoryCopy = true,
                                    copyRate = copyRatePerSecond,
                                    pathView = pathView.await(),
                                    totalBytesCopied = totalBytesCopied,
                                    operation = operation,
                                    sourceDirectoryPathIfDirectoryCopy = directoryFile.absolutePath
                                )

                            }
                        }
                    }

                    override suspend fun onFileExists(sourceFilePath: String?, destinationFilePath: String?) {
                        val input = sourceFilePath ?: directoryFile.absolutePath
                        val output = destinationFilePath ?: destinationDirectoryFile.absolutePath
                        coroutineScope.launch {
                            launch {
                                fileOperationsMutex.withLock {
                                    _fileOperationsProgress.remove(uid)
                                }
                            }
                            launch {
                                _skippedOperations.add(
                                    SkippedOperation(
                                        sourceFilePath = input,
                                        destinationFilePath = output,
                                        intent = if (File(input).isFile) {
                                            if (operation.isCopy()) FileOperation.COPY_FILE else FileOperation.MOVE_FILE
                                        } else operation,
                                    )
                                )
                            }
                        }
                    }

                    override suspend fun onOperationDone(totalFileSize: Long, timeTaken: Long, sourceFilePath: String?, destinationFilePath: String?) {
                        Timber.d("Directory copied successfully")
                        coroutineScope.launch {
                            launch {
                                _events.emit(
                                    FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                                        status = FileOperationsAndroidService.Status.SUCCESS,
                                        uid = uid,
                                        operation = operation
                                    )
                                )
                            }
                            launch {
                                fileOperationsMutex.withLock {
                                    Timber.d("Removing $uid from fileOperationsProgress")
                                    _fileOperationsProgress.remove(uid)
                                }
                            }
                        }
                        jobsEntries.remove(uid)
                    }

                    override suspend fun onOperationNotPermitted(sourceFilePath: String?, destinationFilePath: String?) {
                        super.onOperationNotPermitted(sourceFilePath, destinationFilePath)
                        Timber.d("Operation not permitted 2")
                        _events.emit(
                            FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                                status = FileOperationsAndroidService.Status.NOT_PERMITTED,
                                uid = uid,
                                operation = operation
                            )
                        )
                    }

                },
                controller,
            ).also {
                if (it && operation == FileOperation.MOVE_DIRECTORY && destinationDirectoryFile.path != directoryFile.path) {
                    deleteDirectorySync(directoryPath, uid)
                }
            }
        }.onFailure {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.ERROR,
                    uid = uid,
                    operation = operation
                )
            )
            jobsEntries.remove(uid)
        }.onSuccess {
            if (it) {
                _events.emit(
                    FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                        status = FileOperationsAndroidService.Status.SUCCESS,
                        uid = uid,
                        operation = operation
                    )
                )
            }
            jobsEntries.remove(uid)
        }.getOrDefault(false)
    }

    override fun createDirectory(directoryPath: String, uid: String) {
        val job = coroutineScope.launch {
            kotlin.runCatching {
                fileOperationsService.createDirectory(directoryPath)
            }.onFailure {
                _events.emit(
                    FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                        status = FileOperationsAndroidService.Status.ERROR,
                        uid = uid,
                        operation = FileOperation.CREATE_DIRECTORY
                    )
                )
                jobsEntries.remove(uid)
            }.onSuccess {

                _events.emit(
                    FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                        status = FileOperationsAndroidService.Status.SUCCESS,
                        uid = uid,
                        operation = FileOperation.CREATE_DIRECTORY
                    )
                )

                jobsEntries.remove(uid)
            }
        }

        jobsEntries[uid] = job
    }

    override suspend fun createDirectorySync(directoryPath: String, uid: String) {
        kotlin.runCatching {
            fileOperationsService.createDirectory(directoryPath)
        }.onFailure {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.ERROR,
                    uid = uid,
                    operation = FileOperation.CREATE_DIRECTORY
                )
            )
            jobsEntries.remove(uid)
        }.onSuccess {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.SUCCESS,
                    uid = uid,
                    operation = FileOperation.CREATE_DIRECTORY
                )
            )
            jobsEntries.remove(uid)
        }
    }

    override fun deleteDirectory(directoryPath: String, uid: String) {
        val job = coroutineScope.launch {
            deleteDirectorySync(directoryPath, uid)
        }

        jobsEntries[uid] = job
    }

    override suspend fun deleteDirectorySync(directoryPath: String, uid: String) {
        kotlin.runCatching {
            fileOperationsService.deleteDirectory(directoryPath)
        }.onFailure {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.ERROR,
                    uid = uid,
                    operation = FileOperation.DELETE_DIRECTORY
                )
            )
            jobsEntries.remove(uid)
        }.onSuccess {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.SUCCESS,
                    uid = uid,
                    operation = FileOperation.DELETE_DIRECTORY
                )
            )
            jobsEntries.remove(uid)
        }
    }

    override fun renameDirectory(directoryPath: String, newDirectoryName: String, uid: String) {
        val job = coroutineScope.launch {
            renameDirectorySync(directoryPath, newDirectoryName, uid)
        }

        jobsEntries[uid] = job
    }

    override suspend fun renameDirectorySync(directoryPath: String, newDirectoryName: String, uid: String) {
        kotlin.runCatching {
            fileOperationsService.renameDirectory(directoryPath, newDirectoryName)
        }.onFailure {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.ERROR,
                    uid = uid,
                    operation = FileOperation.RENAME_DIRECTORY
                )
            )
            jobsEntries.remove(uid)
        }.onSuccess {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.SUCCESS,
                    uid = uid,
                    operation = FileOperation.RENAME_DIRECTORY
                )
            )
            jobsEntries.remove(uid)
        }
    }

    override fun createFile(destinationPath: String, fileName: String, extension: String, uid: String) {
        val job = coroutineScope.launch {
            kotlin.runCatching {
                fileOperationsService.createFile(destinationPath, fileName, extension)
            }.onFailure {
                _events.emit(
                    FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                        status = FileOperationsAndroidService.Status.ERROR,
                        uid = uid,
                        operation = FileOperation.CREATE_FILE
                    )
                )
                jobsEntries.remove(uid)
            }.onSuccess {
                _events.emit(
                    FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                        status = FileOperationsAndroidService.Status.SUCCESS,
                        uid = uid,
                        operation = FileOperation.CREATE_FILE
                    )
                )
                jobsEntries.remove(uid)
            }
        }

        jobsEntries[uid] = job
    }

    override suspend fun createFileSync(destinationPath: String, fileName: String, extension: String, uid: String) {
        kotlin.runCatching {
            fileOperationsService.createFile(destinationPath, fileName, extension)
        }.onFailure {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.ERROR,
                    uid = uid,
                    operation = FileOperation.CREATE_FILE
                )
            )
            jobsEntries.remove(uid)
        }.onSuccess {
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.SUCCESS,
                    uid = uid,
                    operation = FileOperation.CREATE_FILE
                )
            )
            jobsEntries.remove(uid)
        }
    }

    override fun stopAll() {
        stopByUids(*(jobsEntries.keys.toTypedArray()))
    }

    override fun stopByUids(vararg uids: String) {
        coroutineScope.launch {
            uids.forEach { uid ->
                jobsEntries[uid]?.cancel()
                jobsEntries.remove(uid)
                _fileOperationsProgress[uid]?.controller?.cancel()
                _fileOperationsProgress.remove(uid)
            }

            if (_fileOperationsProgress.isEmpty()) _fileOperationsProgress.clear()
            Timber.d("stopByUidsFileOperationsProgress: $fileOperationsProgress")
        }
    }

    override fun pauseByUids(vararg uids: String) {
        coroutineScope.launch {
            uids.map {
                async {
                    _fileOperationsProgress[it]?.controller?.pause()
                }
            }.awaitAll()
        }
    }

    override fun resumeByUids(vararg uids: String) {
        coroutineScope.launch {
            uids.map {
                async {
                    _fileOperationsProgress[it]?.controller?.resume()
                }
            }.awaitAll()
        }
    }

    override suspend fun onFileOperationEvent(onEvent: suspend (FileOperationsAndroidService.FileOperationsEvent) -> Unit) {
        _events.filterIsInstance<FileOperationsAndroidService.FileOperationsEvent>()
            .collect {
                coroutineContext.ensureActive()
                onEvent(it)
            }
    }


    override fun resolveSkippedOperation(uid: String, updatedAction: FileExistsAction, remembered: Boolean) {
        val job = coroutineScope.launch {
            resolveSkippedOperationSync(uid, updatedAction, remembered)
            jobsEntries.remove(uid)
            _events.emit(
                FileOperationsAndroidService.FileOperationsEvent.ResultEvent(
                    status = FileOperationsAndroidService.Status.SUCCESS,
                    uid = uid,
                    operation = FileOperation.RESOLVE_SKIPPED_OPERATION
                )
            )
        }

        jobsEntries[uid] = job
    }

    override suspend fun resolveSkippedOperationSync(uid: String, updatedAction: FileExistsAction, remembered: Boolean): Unit = coroutineScope {
        suspend fun resolve(op: SkippedOperation, updatedAction: FileExistsAction) {
            var operation = op.copy(destinationFilePath = File(op.destinationFilePath).parentFile?.absolutePath ?: op.destinationFilePath)
            Timber.d("The skipped list is $_skippedOperations")
            Timber.d("OnResolve $operation with $updatedAction")
            if (updatedAction == FileExistsAction.RENAME && File(op.destinationFilePath).isDirectory) {
                val it = renameFileIfExists(File(op.destinationFilePath))
                operation = operation.copy(destinationFilePath = it.absolutePath)
            }
            if (updatedAction == FileExistsAction.SKIP) return
            when (operation.intent) {
                FileOperation.COPY_FILE -> {
                    copyFileSync(
                        sourceFilePath = operation.sourceFilePath,
                        destinationDirectoryPath = operation.destinationFilePath,
                        fileExistsAction = updatedAction,
                        uid = uid
                    )
                }

                FileOperation.MOVE_FILE -> {
                    moveFileSync(
                        sourceFilePath = operation.sourceFilePath,
                        destinationDirectoryPath = operation.destinationFilePath,
                        fileExistsAction = updatedAction,
                        uid = uid
                    )
                }

                FileOperation.COPY_DIRECTORY -> {
                    copyDirectorySync(
                        directoryPath = operation.sourceFilePath,
                        destinationDirectoryPath = operation.destinationFilePath,
                        fileExistsAction = FileExistsAction.REPORT_IF_EXISTS,
                        uid = uid
                    )
                }

                FileOperation.MOVE_DIRECTORY -> {
                    moveDirectorySync(
                        directoryPath = operation.sourceFilePath,
                        destinationDirectoryPath = operation.destinationFilePath,
                        fileExistsAction = FileExistsAction.REPORT_IF_EXISTS,
                        uid = uid
                    )
                }

                else -> Unit
            }
        }


        if (remembered) {
            val clonedOperations = _skippedOperations.toList()
            _skippedOperations.clear()
            clonedOperations.map {
                async {
                    resolve(it, updatedAction)
                }
            }.awaitAll()
        } else {
            val operations = _skippedOperations.fastFilter { it.uid == uid }
            _skippedOperations.removeIf { it.uid == uid }
            operations.map {
                async {
                    resolve(it, updatedAction)
                }
            }.awaitAll()

        }


    }

    // ClipboardExecutor
    override fun execute(payload: ClipboardEntryPayload, intent: ClipboardEntry.Intent, destinationPath: String) {
        coroutineScope.launch {
            val isSuccess = executeSync(payload, intent, destinationPath)
            _events.emit(
                ClipboardExecutor.ClipboardExecutionEvent.PayloadExecutionResultEvent(
                    payload = payload,
                    status = if (isSuccess) ClipboardExecutor.ExecutionStatus.SUCCESS
                    else ClipboardExecutor.ExecutionStatus.ERROR
                )
            )
        }

    }

    override fun execute(clipboardEntry: ClipboardEntry, destinationPath: String) {
        val job = coroutineScope.launch {
            val isSuccess = clipboardEntry.payloads.map { payload ->
                async {
                    executeSync(payload, clipboardEntry.intent, destinationPath)
                }
            }.awaitAll().all { it }
            _events.emit(
                ClipboardExecutor.ClipboardExecutionEvent.EntryExecutionResultEvent(
                    clipboardEntry = clipboardEntry,
                    status = if (isSuccess) ClipboardExecutor.ExecutionStatus.SUCCESS else ClipboardExecutor.ExecutionStatus.ERROR
                )
            )
        }

        jobsEntries[clipboardEntry.uid] = job
    }

    override suspend fun executeSync(clipboardEntry: ClipboardEntry, destinationPath: String): Boolean {
        var isSuccess = false
        val job = coroutineScope.launch {
            isSuccess = clipboardEntry.payloads.map { payload ->
                async {
                    executeSync(payload, clipboardEntry.intent, destinationPath)
                }
            }.awaitAll().all { it }
            jobsEntries.remove(clipboardEntry.uid)
        }

        jobsEntries[clipboardEntry.uid] = job
        job.join()
        return isSuccess
    }

    override suspend fun executeSync(payload: ClipboardEntryPayload, intent: ClipboardEntry.Intent, destinationPath: String): Boolean {
        Timber.d("executeSync: payload $payload")
        Timber.d("executeSync: intent $intent")
        Timber.d("executeSync: ${payload.mediaInfo.privateContentUri?.toFile()?.absolutePath} --> $destinationPath")
        var isSuccess = false
        val job = coroutineScope.launch {
            val file = payload.mediaInfo.privateContentUri!!.toFile()
            Timber.d("executeSync: the final file is ${File(destinationPath, file.name).absolutePath}")
            isSuccess = when (intent) {
                ClipboardEntry.Intent.CUT -> {
                    if (file.isFile) moveFileSync(
                        sourceFilePath = file.absolutePath,
                        destinationDirectoryPath = destinationPath,
                        uid = payload.uid
                    )
                    else moveDirectorySync(
                        directoryPath = file.absolutePath,
                        destinationDirectoryPath = File(destinationPath, file.name).absolutePath,
                        uid = payload.uid
                    )
                }

                ClipboardEntry.Intent.COPY -> {
                    if (file.isFile) copyFileSync(
                        sourceFilePath = file.absolutePath,
                        destinationDirectoryPath = destinationPath,
                        uid = payload.uid
                    )
                    else copyDirectorySync(
                        directoryPath = file.absolutePath,
                        destinationDirectoryPath = File(destinationPath, file.name).absolutePath,
                        uid = payload.uid
                    )
                }
            }
            jobsEntries.remove(payload.uid)
        }
        jobsEntries[payload.uid] = job
        job.join()
        return isSuccess
    }

    override fun stopAll(entries: List<ClipboardEntry>) {
        val uids = mutableSetOf<String>()
        coroutineScope.launch {
            entries.forEach { entry ->
                entry.payloads.forEach { payload ->
                    uids.add(payload.uid)
                }
                uids.add(entry.uid)
            }
        }

        stopByUids(*uids.toTypedArray())
    }


    override fun stopByClipboardEntry(clipboardEntry: ClipboardEntry) {
        coroutineScope.launch {
            clipboardEntry.payloads.forEach { payload ->
                jobsEntries[payload.uid]?.cancel()
            }
            jobsEntries[clipboardEntry.uid]?.cancel()
        }
    }

    override suspend fun onExecutionEvent(onEvent: suspend (ClipboardExecutor.ClipboardExecutionEvent) -> Unit) {
        coroutineScope.launch {
            _events.filterIsInstance<ClipboardExecutor.ClipboardExecutionEvent>()
                .collectLatest {
                    coroutineContext.ensureActive()
                    onEvent(it)
                }
        }
    }


}