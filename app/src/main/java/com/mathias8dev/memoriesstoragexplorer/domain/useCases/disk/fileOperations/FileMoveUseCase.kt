package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations.FileOperationsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class FileMoveUseCase(
    private val fileOperationsService: FileOperationsService
) {

    suspend operator fun invoke(sourceFilePath: String, destinationFilePath: String) = withContext(Dispatchers.IO) {
        fileOperationsService.moveFile(sourceFilePath, destinationFilePath)
    }

    suspend operator fun invoke(sourceFile: File, destinationFile: File) = withContext(Dispatchers.IO) {
        fileOperationsService.moveFile(sourceFile, destinationFile)
    }
}
