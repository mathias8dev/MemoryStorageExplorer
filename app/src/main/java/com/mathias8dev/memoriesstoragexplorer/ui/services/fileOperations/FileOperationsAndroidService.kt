package com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations

import com.mathias8dev.memoriesstoragexplorer.data.event.Event
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FileExistsAction
import java.util.UUID


interface FileOperationsAndroidService {

    val fileOperationsProgress: Map<String, FileOperationProgress>
    val skippedOperations: List<SkippedOperation>

    fun copyFile(sourceFilePath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction = FileExistsAction.REPORT_IF_EXISTS, uid: String = UUID.randomUUID().toString())
    suspend fun copyFileSync(
        sourceFilePath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction = FileExistsAction.REPORT_IF_EXISTS,
        uid: String = UUID.randomUUID().toString()
    ): Boolean

    fun moveFile(sourceFilePath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction = FileExistsAction.REPORT_IF_EXISTS, uid: String = UUID.randomUUID().toString())
    suspend fun moveFileSync(
        sourceFilePath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction = FileExistsAction.REPORT_IF_EXISTS,
        uid: String = UUID.randomUUID().toString()
    ): Boolean

    fun silentDeleteFile(filePath: String, uid: String = UUID.randomUUID().toString())
    suspend fun silentDeleteFileSync(filePath: String, uid: String = UUID.randomUUID().toString())
    fun renameFile(filePath: String, newFileName: String, uid: String = UUID.randomUUID().toString())
    suspend fun renameFileSync(filePath: String, newFileName: String, uid: String = UUID.randomUUID().toString())
    fun copyDirectory(directoryPath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction = FileExistsAction.REPORT_IF_EXISTS, uid: String = UUID.randomUUID().toString())
    suspend fun copyDirectorySync(
        directoryPath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction = FileExistsAction.REPORT_IF_EXISTS,
        uid: String = UUID.randomUUID().toString()
    ): Boolean


    fun moveDirectory(directoryPath: String, destinationDirectoryPath: String, fileExistsAction: FileExistsAction = FileExistsAction.REPORT_IF_EXISTS, uid: String = UUID.randomUUID().toString())
    suspend fun moveDirectorySync(
        directoryPath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction = FileExistsAction.REPORT_IF_EXISTS,
        uid: String = UUID.randomUUID().toString()
    ): Boolean


    fun createDirectory(directoryPath: String, uid: String = UUID.randomUUID().toString())
    suspend fun createDirectorySync(directoryPath: String, uid: String = UUID.randomUUID().toString())
    fun deleteDirectory(directoryPath: String, uid: String = UUID.randomUUID().toString())
    suspend fun deleteDirectorySync(directoryPath: String, uid: String = UUID.randomUUID().toString())
    fun renameDirectory(directoryPath: String, newDirectoryName: String, uid: String = UUID.randomUUID().toString())
    suspend fun renameDirectorySync(directoryPath: String, newDirectoryName: String, uid: String = UUID.randomUUID().toString())
    fun createFile(destinationPath: String, fileName: String, extension: String, uid: String = UUID.randomUUID().toString())
    suspend fun createFileSync(destinationPath: String, fileName: String, extension: String, uid: String = UUID.randomUUID().toString())

    fun stopAll()
    fun stopByUids(vararg uids: String)
    fun pauseByUids(vararg uids: String)
    fun resumeByUids(vararg uids: String)

    suspend fun onFileOperationEvent(onEvent: suspend (FileOperationsEvent) -> Unit)

    fun resolveSkippedOperation(uid: String, updatedAction: FileExistsAction = FileExistsAction.SKIP, remembered: Boolean = false)
    suspend fun resolveSkippedOperationSync(uid: String, updatedAction: FileExistsAction = FileExistsAction.SKIP, remembered: Boolean = false)

    sealed class FileOperationsEvent : Event() {
        data class ResultEvent(
            val status: Status,
            val uid: String,
            val operation: FileOperation = FileOperation.UNKNOWN
        ) : FileOperationsEvent()

        data class ProgressEvent(
            val progress: Int,
            val uid: String,
            val operation: FileOperation = FileOperation.UNKNOWN
        ) : FileOperationsEvent()
    }

    enum class Status {
        SUCCESS,
        ERROR,
        NOT_PERMITTED,
    }

}
