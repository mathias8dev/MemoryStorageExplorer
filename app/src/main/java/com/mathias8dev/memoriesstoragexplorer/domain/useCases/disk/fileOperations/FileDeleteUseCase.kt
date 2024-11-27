package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations.FileOperationsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class FileDeleteUseCase(
    private val fileOperationsService: FileOperationsService
) {

    suspend operator fun invoke(filePath: String) = withContext(Dispatchers.IO) {
        fileOperationsService.deleteFile(filePath)
    }

    suspend operator fun invoke(file: File) = withContext(Dispatchers.IO) {
        fileOperationsService.deleteFile(file)
    }
}
