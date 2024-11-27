package com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations

import java.util.UUID


data class SkippedOperation(
    val uid: String = UUID.randomUUID().toString(),
    val sourceFilePath: String,
    val destinationFilePath: String,
    val intent: FileOperation,
)