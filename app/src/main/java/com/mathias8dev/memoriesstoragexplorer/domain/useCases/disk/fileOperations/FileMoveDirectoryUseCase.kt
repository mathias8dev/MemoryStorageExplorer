package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations.FileOperationsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class FileMoveDirectoryUseCase(
    private val fileOperationsService: FileOperationsService
) {

    suspend operator fun invoke(sourceDirectoryPath: String, destinationDirectoryPath: String) = withContext(Dispatchers.IO) {
        fileOperationsService.moveDirectory(sourceDirectoryPath, destinationDirectoryPath)
    }

    suspend operator fun invoke(sourceDirectory: File, destinationDirectory: File) = withContext(Dispatchers.IO) {
        fileOperationsService.moveDirectory(sourceDirectory, destinationDirectory)
    }
}