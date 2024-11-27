package com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy


interface FastFileCopyListener {
    suspend fun onProgressUpdate(
        bytesCopied: Long,
        totalFileSize: Long,
        sourceFilePath: String? = null,
        destinationFilePath: String? = null
    ) {
        print("Progress update: $sourceFilePath --> $destinationFilePath")
    }

    suspend fun onFileExists(
        sourceFilePath: String? = null,
        destinationFilePath: String? = null
    ) {
        print("File exists: $sourceFilePath --> $destinationFilePath")
    }

    suspend fun onOperationDone(
        totalFileSize: Long,
        timeTaken: Long,
        sourceFilePath: String? = null,
        destinationFilePath: String? = null
    ) {
        print("Operation done: $sourceFilePath --> $destinationFilePath")
    }

    suspend fun onOperationCancelled(
        sourceFilePath: String? = null,
        destinationFilePath: String? = null
    ) {
        print("Operation cancelled: $sourceFilePath --> $destinationFilePath")
    }

    suspend fun onOperationFailed(
        throwable: Throwable,
        sourceFilePath: String? = null,
        destinationFilePath: String? = null
    ) {
        print("Operation failed: $sourceFilePath --> $destinationFilePath")
    }

    suspend fun onOperationNotPermitted(
        sourceFilePath: String? = null,
        destinationFilePath: String? = null
    ) {
        print("Operation not permitted: $sourceFilePath --> $destinationFilePath")
    }
}