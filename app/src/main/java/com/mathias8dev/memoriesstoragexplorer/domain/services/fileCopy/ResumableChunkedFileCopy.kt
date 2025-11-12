package com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import timber.log.Timber
import java.io.RandomAccessFile


class ResumableChunkedFileCopy(
    private val inputFilePath: String,
    private val outputFilePath: String,
    private val startByte: Long,
    private val endByte: Long,
    private val totalFileSize: Long,
    private val fastFileCopyListener: FastFileCopyListener? = null,
    private val controller: FastFileCopyController? = null
) {

    suspend fun copyChunk() = coroutineScope {
        val inputFile = RandomAccessFile(inputFilePath, "r")
        val outputFile = RandomAccessFile(outputFilePath, "rw")

        try {
            inputFile.seek(startByte)
            outputFile.seek(startByte)

            val buffer = ByteArray(4096)
            var bytesRead = 0L
            var totalBytesCopied = startByte

            while (totalBytesCopied <= endByte && inputFile.read(buffer).also { bytesRead = it.toLong() } != -1) {
                if (controller?.isCancelled() == true) {
                    Timber.d("On operation cancelled")
                    fastFileCopyListener?.onOperationCancelled(inputFilePath, outputFilePath)
                    break
                }

                while (controller?.isPaused() == true) {
                    delay(1000) // Check every 1000ms if the operation should resume
                }

                val bytesToWrite = minOf(bytesRead, endByte - totalBytesCopied + 1)
                outputFile.write(buffer, 0, bytesToWrite.toInt())
                totalBytesCopied += bytesToWrite
                fastFileCopyListener?.onProgressUpdate(bytesRead, totalFileSize, inputFilePath, outputFilePath)
            }
        } finally {
            // RESOURCE LEAK FIX: Always close files even if exception occurs
            try {
                inputFile.close()
            } catch (e: Exception) {
                Timber.e(e, "Error closing input file")
            }
            try {
                outputFile.close()
            } catch (e: Exception) {
                Timber.e(e, "Error closing output file")
            }
        }
    }
}