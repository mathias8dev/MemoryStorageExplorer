package com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations

import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FastFileCopyController
import com.mathias8dev.memoriesstoragexplorer.ui.composables.SelectedPathView
import java.time.LocalDateTime


data class FileOperationProgress(
    val sourceFilePath: String,
    val destinationFilePath: String,
    val progress: Float,
    val copyRate: Long = 0,
    val totalBytesCopied: Long = 0,
    val controller: FastFileCopyController? = null,
    val isDirectoryCopy: Boolean = false,
    val sourceDirectoryPathIfDirectoryCopy: String? = null,
    val pathView: SelectedPathView? = null,
    val operation: FileOperation = FileOperation.UNKNOWN,
    val insertedDate: LocalDateTime = LocalDateTime.now()
)