package com.mathias8dev.memoriesstoragexplorer.ui.composables.autoGrow

import java.util.UUID


data class AutoGrowTab(
    val title: String,
    val id: String = UUID.randomUUID().toString()
)