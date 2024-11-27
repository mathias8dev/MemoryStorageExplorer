package com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FastFileCopyController
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FastFileCopyListener
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FileExistsAction
import java.io.File
import java.io.IOException

interface FileOperationsService {

    @Throws(IOException::class)
    fun copyFile(sourceFilePath: String, destinationFilePath: String): Boolean

    @Throws(IOException::class)
    fun copyFile(sourceFile: File, destinationFile: File): Boolean

    @Throws(IOException::class)
    suspend fun fastCopyFile(
        sourceFilePath: String,
        destinationFilePath: String,
        fileExistsAction: FileExistsAction = FileExistsAction.RENAME,
        fastFileCopyListener: FastFileCopyListener? = null,
        controller: FastFileCopyController? = null
    ): Boolean

    @Throws(IOException::class)
    suspend fun fastCopyFile(
        sourceFile: File,
        destinationFile: File,
        fileExistsAction: FileExistsAction = FileExistsAction.RENAME,
        fastFileCopyListener: FastFileCopyListener? = null,
        controller: FastFileCopyController? = null
    ): Boolean

    @Throws(IOException::class)
    fun moveFile(sourceFilePath: String, destinationFilePath: String): Boolean

    @Throws(IOException::class)
    fun moveFile(sourceFile: File, destinationFile: File)

    @Throws(IOException::class)
    suspend fun fastMoveFile(
        sourceFilePath: String,
        destinationFilePath: String,
        fileExistsAction: FileExistsAction = FileExistsAction.RENAME,
        fastFileCopyListener: FastFileCopyListener? = null,
        controller: FastFileCopyController? = null
    )

    @Throws(IOException::class)
    suspend fun fastMoveFile(
        sourceFile: File,
        destinationFile: File,
        fileExistsAction: FileExistsAction = FileExistsAction.RENAME,
        fastFileCopyListener: FastFileCopyListener? = null,
        controller: FastFileCopyController? = null
    )

    @Throws(IOException::class)
    fun deleteFile(filePath: String): Boolean

    @Throws(IOException::class)
    fun deleteFile(file: File): Boolean

    @Throws(IOException::class)
    fun renameFile(filePath: String, newFileName: String)

    @Throws(IOException::class)
    fun renameFile(file: File, newFileName: String)

    @Throws(IOException::class)
    fun createDirectory(directoryPath: String)

    @Throws(IOException::class)
    fun createDirectory(directory: File)

    @Throws(IOException::class)
    fun deleteDirectory(directoryPath: String): Boolean

    @Throws(IOException::class)
    fun deleteDirectory(directory: File): Boolean

    @Throws(IOException::class)
    fun renameDirectory(directoryPath: String, newDirectoryName: String)

    @Throws(IOException::class)
    fun renameDirectory(directory: File, newDirectoryName: String)

    @Throws(IOException::class)
    fun moveDirectory(sourceDirectoryPath: String, destinationDirectoryPath: String)

    @Throws(IOException::class)
    fun moveDirectory(sourceDirectory: File, destinationDirectory: File)

    @Throws(IOException::class)
    suspend fun fastMoveDirectory(
        sourceDirectoryPath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction = FileExistsAction.RENAME,
        fastFileCopyListener: FastFileCopyListener? = null,
        controller: FastFileCopyController? = null,
    ): Boolean

    @Throws(IOException::class)
    suspend fun fastMoveDirectory(
        sourceDirectory: File,
        destinationDirectory: File,
        fileExistsAction: FileExistsAction = FileExistsAction.RENAME,
        fastFileCopyListener: FastFileCopyListener? = null,
        controller: FastFileCopyController? = null,
    ): Boolean

    @Throws(IOException::class)
    fun copyDirectory(sourceDirectoryPath: String, destinationDirectoryPath: String)


    @Throws(IOException::class)
    fun copyDirectory(sourceDirectory: File, destinationDirectory: File)

    @Throws(IOException::class)
    suspend fun fastCopyDirectory(
        sourceDirectoryPath: String,
        destinationDirectoryPath: String,
        fileExistsAction: FileExistsAction = FileExistsAction.RENAME,
        fastFileCopyListener: FastFileCopyListener? = null,
        controller: FastFileCopyController? = null,
    ): Boolean

    @Throws(IOException::class)
    suspend fun fastCopyDirectory(
        sourceDirectory: File,
        destinationDirectory: File,
        fileExistsAction: FileExistsAction = FileExistsAction.RENAME,
        fastFileCopyListener: FastFileCopyListener? = null,
        controller: FastFileCopyController? = null,
    ): Boolean

    @Throws(IOException::class)
    fun createFile(destinationPath: String, fileName: String, extension: String)
}