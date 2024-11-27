package com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy

object FileCopyUtils {
    suspend fun copy(sourceFilePath: String, destinationFilePath: String, fastFileCopyListener: FastFileCopyListener? = null, controller: FastFileCopyController? = null) {
        val fastFileCopy = FastFileCopy(sourceFilePath, destinationFilePath, fastFileCopyListener, controller)
        fastFileCopy.copy()
    }
}