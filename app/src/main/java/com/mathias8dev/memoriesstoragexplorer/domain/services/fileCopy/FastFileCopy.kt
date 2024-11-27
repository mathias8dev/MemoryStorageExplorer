package com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy

import com.mathias8dev.memoriesstoragexplorer.domain.utils.renameFileIfExists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.util.UUID


class FastFileCopy(
    private val inputFilePath: String,
    private val outputFilePath: String,
    private val fastFileCopyListener: FastFileCopyListener? = null,
    private val controller: FastFileCopyController? = null
) {

    suspend fun copy(fileExistsAction: FileExistsAction = FileExistsAction.RENAME) = withContext(Dispatchers.IO) {
        Timber.d("Copying file: $inputFilePath to $outputFilePath with action: $fileExistsAction")
        val inputFile = File(inputFilePath)
        var outputFile = File(outputFilePath)


        when {
            outputFile.exists() && fileExistsAction == FileExistsAction.REPORT_IF_EXISTS -> {
                fastFileCopyListener?.onFileExists(inputFilePath, outputFilePath)
                return@withContext false
            }

            outputFile.exists() && fileExistsAction == FileExistsAction.SKIP -> {
                return@withContext false
            }

            outputFile.exists() && fileExistsAction == FileExistsAction.RENAME -> {
                outputFile = renameFileIfExists(outputFile)
            }

            outputFile.exists() && fileExistsAction == FileExistsAction.OVERWRITE -> {
                // Since the output file and the input file are the same, copy to a temp file and rename later
                outputFile = File(outputFile.parent, "${UUID.randomUUID()}.tmp")
            }
        }

        val fileSize = inputFile.length()
        val numThreads = fileSize.div(BASE_SIZE_PER_THREAD).coerceIn(MIN_THREADS, MAX_THREADS)

        val chunkSize = fileSize / numThreads

        val startTime = System.currentTimeMillis()
        val deferred = (0 until numThreads).map { i ->
            async(Dispatchers.IO) {
                val startByte = i * chunkSize
                val endByte = if (i == numThreads - 1) fileSize - 1 else (startByte + chunkSize - 1)
                val copier = ResumableChunkedFileCopy(inputFilePath, outputFile.absolutePath, startByte, endByte, fileSize, fastFileCopyListener, controller)
                copier.copyChunk()
            }
        }

        kotlin.runCatching { deferred.awaitAll() }.onFailure {
            if (outputFile.exists()) outputFile.delete()

            controller?.cancel()
            fastFileCopyListener?.onOperationCancelled(inputFilePath, outputFilePath)
            fastFileCopyListener?.onOperationFailed(it, inputFilePath, outputFilePath)
        }.onSuccess {
            if (fileExistsAction == FileExistsAction.OVERWRITE) {
                val originalOutputFile = File(outputFilePath)
                originalOutputFile.delete()
                outputFile.renameTo(originalOutputFile)
                outputFile = originalOutputFile
            }
        }


        if (fileSize == outputFile.length()) {
            fastFileCopyListener?.onOperationDone(fileSize, System.currentTimeMillis() - startTime, inputFilePath, outputFilePath)
            true
        } else {
            if (outputFile.exists()) outputFile.delete()

            fastFileCopyListener?.onOperationFailed(Exception("File copy failed: $inputFilePath to $outputFilePath"), inputFilePath, outputFilePath)
            false
        }
    }

    companion object {
        private const val BASE_SIZE_PER_THREAD = 10 * 1024 * 1024L // 10 MB
        private const val MIN_THREADS = 1L
        private const val MAX_THREADS = 8L
    }
}