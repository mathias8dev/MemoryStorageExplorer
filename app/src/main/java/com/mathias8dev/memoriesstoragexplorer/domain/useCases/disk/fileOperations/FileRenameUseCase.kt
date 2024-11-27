package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations.FileOperationsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class FileRenameUseCase(
    private val fileOperationsService: FileOperationsService
) {

    suspend operator fun invoke(filePath: String, newFileName: String) = withContext(Dispatchers.IO) {
        fileOperationsService.renameFile(filePath, newFileName)
    }

    suspend operator fun invoke(file: File, newFileName: String) = withContext(Dispatchers.IO) {
        fileOperationsService.renameFile(file, newFileName)
    }
}