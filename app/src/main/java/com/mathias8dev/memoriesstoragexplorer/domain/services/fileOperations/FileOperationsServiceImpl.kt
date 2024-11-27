package com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FastFileCopy
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FastFileCopyController
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FastFileCopyListener
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FileExistsAction
import com.mathias8dev.memoriesstoragexplorer.domain.utils.renameFileIfExists
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.File
import java.io.IOException


@Single
class FileOperationsServiceImpl : FileOperationsService {

    @Throws(IOException::class)
    override fun copyFile(sourceFilePath: String, destinationFilePath: String): Boolean {
        val sourceFile = File(sourceFilePath)
        val destinationFile = File(destinationFilePath)

        destinationFile.parentFile?.mkdirs()
        val dest = sourceFile.copyTo(destinationFile, overwrite = true)
        if (dest.length() != sourceFile.length()) {
            dest.delete()
            return false
        }
        return true
    }

    @Throws(IOException::class)
    override fun copyFile(sourceFile: File, destinationFile: File): Boolean {
        destinationFile.parentFile?.mkdirs()
        val dest = sourceFile.copyTo(destinationFile, overwrite = true)
        if (dest.length() != sourceFile.length()) {
            dest.delete()
            return false
        }
        return true
    }

    override suspend fun fastCopyFile(
        sourceFilePath: String,
        destinationFilePath: String,
        fileExistsAction: FileExistsAction,
        fastFileCopyListener: FastFileCopyListener?,
        controller: FastFileCopyController?
    ): Boolean {
        Timber.d("fastCopyFile: $sourceFilePath -> $destinationFilePath with action $fileExistsAction")
        val fastFileCopy = FastFileCopy(sourceFilePath, destinationFilePath, fastFileCopyListener, controller)
        return fastFileCopy.copy(fileExistsAction)
    }

    override suspend fun fastCopyFile(
        sourceFile: File,
        destinationFile: File,
        fileExistsAction: FileExistsAction,
        fastFileCopyListener: FastFileCopyListener?,
        controller: FastFileCopyController?
    ): Boolean {
        return fastCopyFile(
            sourceFile.absolutePath,
            destinationFile.absolutePath,
            fileExistsAction,
            fastFileCopyListener,
            controller
        )
    }

    @Throws(IOException::class)
    override fun moveFile(sourceFilePath: String, destinationFilePath: String): Boolean {
        if (!copyFile(sourceFilePath, destinationFilePath)) {
            return false
        }
        return deleteFile(sourceFilePath)
    }

    @Throws(IOException::class)
    override fun moveFile(sourceFile: File, destinationFile: File) {
        copyFile(sourceFile, destinationFile)
        deleteFile(sourceFile)
    }

    override suspend fun fastMoveFile(
        sourceFilePath: String,
        destinationFilePath: String,
        fileExistsAction: FileExistsAction,
        fastFileCopyListener: FastFileCopyListener?,
        controller: FastFileCopyController?
    ) {
        fastCopyFile(sourceFilePath, destinationFilePath, fileExistsAction, fastFileCopyListener, controller)
        deleteFile(sourceFilePath)
    }

    override suspend fun fastMoveFile(
        sourceFile: File,
        destinationFile: File,
        fileExistsAction: FileExistsAction,
        fastFileCopyListener: FastFileCopyListener?,
        controller: FastFileCopyController?
    ) {
        fastCopyFile(sourceFile, destinationFile, fileExistsAction, fastFileCopyListener, controller)
        deleteFile(sourceFile)
    }

    @Throws(IOException::class)
    override fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return deleteFile(file)
    }

    @Throws(IOException::class)
    override fun deleteFile(file: File): Boolean {
        return file.delete()
    }

    @Throws(IOException::class)
    override fun renameFile(filePath: String, newFileName: String) {
        val file = File(filePath)
        renameFile(file, newFileName)
    }

    @Throws(IOException::class)
    override fun renameFile(file: File, newFileName: String) {
        val parentDir = file.parentFile
        var newFile = File(parentDir, newFileName)

        if (newFile.exists()) newFile = renameFileIfExists(newFile)

        if (!file.renameTo(newFile)) {
            throw IOException("Failed to rename file: ${file.absolutePath} to ${newFile.absolutePath}")
        }
    }

    @Throws(IOException::class)
    override fun createDirectory(directoryPath: String) {
        val directory = File(directoryPath)
        createDirectory(directory)
    }

    @Throws(IOException::class)
    override fun createDirectory(directory: File) {
        var file = directory
        if (file.exists()) file = renameFileIfExists(file)
        if (!file.mkdirs() && !file.exists()) {
            throw IOException("Failed to create directory: ${file.absolutePath}")
        }
    }

    @Throws(IOException::class)
    override fun deleteDirectory(directoryPath: String): Boolean {
        val directory = File(directoryPath)
        return deleteDirectory(directory)
    }

    @Throws(IOException::class)
    override fun deleteDirectory(directory: File): Boolean {
        return directory.deleteRecursively()
    }

    @Throws(IOException::class)
    override fun renameDirectory(directoryPath: String, newDirectoryName: String) {
        val directory = File(directoryPath)
        renameDirectory(directory, newDirectoryName)
    }

    @Throws(IOException::class)
    override fun renameDirectory(directory: File, newDirectoryName: String) {
        val newDirectory = File(directory.parent, newDirectoryName)
        if (!directory.renameTo(newDirectory)) {
            throw IOException("Failed to rename directory: ${directory.absolutePath} to $newDirectoryName")
        }
    }

    @Throws(IOException::class)
    override fun moveDirectory(sourceDirectoryPath: String, destinationDirectoryPath: String) {
        copyDirectory(sourceDirectoryPath, destinationDirectoryPath)
        deleteDirectory(sourceDirectoryPath)
    }

    @Throws(IOException::class)
    override fun moveDirectory(sourceDirectory: File, destinationDirectory: File) {
        copyDirectory(sourceDirectory, destinationDirectory)
        deleteDirectory(sourceDirectory)
    }

    override suspend fun fastMoveDirectory(
        sourceDirectoryPath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction,
        fastFileCopyListener: FastFileCopyListener?,
        controller: FastFileCopyController?
    ): Boolean {
        val sourceDirectory = File(sourceDirectoryPath)
        val destinationDirectory = File(destinationDirectoryPath)
        return fastMoveDirectory(sourceDirectory, destinationDirectory, fileExistsAction, fastFileCopyListener, controller)
    }

    override suspend fun fastMoveDirectory(
        sourceDirectory: File,
        destinationDirectory: File,
        fileExistsAction: FileExistsAction,
        fastFileCopyListener: FastFileCopyListener?,
        controller: FastFileCopyController?
    ): Boolean {
        Timber.d("fastMoveDirectory: $sourceDirectory -> $destinationDirectory")
        return if (fastCopyDirectory(sourceDirectory, destinationDirectory, fileExistsAction, fastFileCopyListener, controller)) {
            if (sourceDirectory.path != destinationDirectory.path || (sourceDirectory.path == destinationDirectory.path && fileExistsAction == FileExistsAction.RENAME)) {
                deleteDirectory(sourceDirectory)
            } else true
        } else false
    }

    @Throws(IOException::class)
    override fun copyDirectory(sourceDirectoryPath: String, destinationDirectoryPath: String) {
        val sourceDirectory = File(sourceDirectoryPath)
        val destinationDirectory = File(destinationDirectoryPath)
        copyDirectory(sourceDirectory, destinationDirectory)
    }

    @Throws(IOException::class)
    override fun copyDirectory(sourceDirectory: File, destinationDirectory: File) {
        sourceDirectory.copyRecursively(destinationDirectory, overwrite = true)
    }

    override suspend fun fastCopyDirectory(
        sourceDirectoryPath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction,
        fastFileCopyListener: FastFileCopyListener?,
        controller: FastFileCopyController?
    ): Boolean {
        val sourceDirectory = File(sourceDirectoryPath)
        val destinationDirectory = File(destinationDirectoryPath)
        return fastCopyDirectory(sourceDirectory, destinationDirectory, fileExistsAction, fastFileCopyListener, controller)
    }

    override suspend fun fastCopyDirectory(
        sourceDirectory: File,
        destinationDirectory: File,
        fileExistsAction: FileExistsAction,
        fastFileCopyListener: FastFileCopyListener?,
        controller: FastFileCopyController?
    ): Boolean {

        if (destinationDirectory.parentFile?.absolutePath == sourceDirectory.absolutePath) {
            fastFileCopyListener?.onOperationNotPermitted(sourceDirectory.absolutePath, destinationDirectory.absolutePath)
            return false
        }
        val now = System.currentTimeMillis()
        var totalCopied = 0L
        val totalCopiedMutex = Mutex()
        val listener = object : FastFileCopyListener {
            override suspend fun onProgressUpdate(bytesCopied: Long, totalFileSize: Long, sourceFilePath: String?, destinationFilePath: String?) {
                fastFileCopyListener?.onProgressUpdate(bytesCopied, totalFileSize, sourceFilePath, destinationFilePath)
                totalCopiedMutex.withLock {
                    totalCopied += bytesCopied
                }
            }

            override suspend fun onOperationFailed(throwable: Throwable, sourceFilePath: String?, destinationFilePath: String?) {
                fastFileCopyListener?.onOperationFailed(throwable, sourceFilePath, destinationFilePath)
            }


        }

        return kotlin.runCatching {
            for (src in sourceDirectory.walkTopDown().onFail { _, ioException -> throw ioException }) {
                if (src.exists()) {
                    val relPath = src.toRelativeString(sourceDirectory)
                    val dstFile = File(destinationDirectory, relPath)
                    Timber.d("Copy ${sourceDirectory.absolutePath} -> ${dstFile.absolutePath}")
                    if (dstFile.isDirectory && dstFile.absolutePath == sourceDirectory.absolutePath && fileExistsAction == FileExistsAction.REPORT_IF_EXISTS) {
                        Timber.d("Source and destination are the same")
                        fastFileCopyListener?.onFileExists(src.absolutePath, dstFile.absolutePath)
                        break
                    }
                    if (dstFile.exists() && fileExistsAction == FileExistsAction.REPORT_IF_EXISTS) {
                        fastFileCopyListener?.onFileExists(src.absolutePath, dstFile.absolutePath)
                        continue
                    }
                    if (dstFile.exists() && fileExistsAction == FileExistsAction.SKIP) continue

                    if (dstFile.exists() && dstFile.absolutePath != src.absolutePath && !(src.isDirectory && dstFile.isDirectory)) {
                        val stillExists = if (fileExistsAction != FileExistsAction.OVERWRITE) true else {
                            if (dstFile.isDirectory) !dstFile.deleteRecursively()
                            else !dstFile.delete()
                        }


                        if (stillExists && fileExistsAction == FileExistsAction.OVERWRITE) {
                            fastFileCopyListener?.onOperationFailed(IOException("Failed to copy file: ${src.absolutePath}"), sourceDirectory.absolutePath, destinationDirectory.absolutePath)
                            continue
                        }
                    }

                    if (src.isDirectory) {
                        dstFile.mkdirs()
                    } else {
                        if (!fastCopyFile(src, dstFile, fileExistsAction, listener, controller)) {
                            fastFileCopyListener?.onOperationFailed(IOException("Failed to copy file: ${src.absolutePath}"), sourceDirectory.absolutePath, destinationDirectory.absolutePath)
                            continue
                        }
                    }
                }
            }

            fastFileCopyListener?.onOperationDone(totalCopied, System.currentTimeMillis() - now, sourceDirectory.absolutePath, destinationDirectory.absolutePath)
            true
        }.onFailure {
            fastFileCopyListener?.onOperationFailed(it, sourceDirectory.absolutePath, destinationDirectory.absolutePath)
            return false
        }.getOrDefault(false)

    }

    override fun createFile(destinationPath: String, fileName: String, extension: String) {
        var file = File(destinationPath, "$fileName.$extension")
        if (file.exists()) file = renameFileIfExists(file)
        file.createNewFile()
    }

}