package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileOperations.FileOperationsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory


@Factory
class FileCreateUseCase(
    private val fileOperationsService: FileOperationsService
) {

    suspend operator fun invoke(destinationPath: String, fileName: String, extension: String) = withContext(Dispatchers.IO){
        fileOperationsService.createFile(destinationPath, fileName, extension)
    }
}