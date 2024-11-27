package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations.FileOperationsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class FileDeleteDirectoryUseCase(
    private val fileOperationsService: FileOperationsService
) {

    suspend operator fun invoke(directoryPath: String) = withContext(Dispatchers.IO) {
        fileOperationsService.deleteDirectory(directoryPath)
    }

    suspend operator fun invoke(directory: File) = withContext(Dispatchers.IO) {
        fileOperationsService.deleteDirectory(directory)
    }
}