package com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations

import com.mathias8dev.memoriesstoragexplorer.data.event.Event
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntry
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntryPayload


interface ClipboardExecutor {
    fun execute(payload: ClipboardEntryPayload, intent: ClipboardEntry.Intent, destinationPath: String)
    suspend fun executeSync(payload: ClipboardEntryPayload, intent: ClipboardEntry.Intent, destinationPath: String): Boolean
    fun execute(clipboardEntry: ClipboardEntry, destinationPath: String)
    suspend fun executeSync(clipboardEntry: ClipboardEntry, destinationPath: String): Boolean
    fun stopAll(entries: List<ClipboardEntry>)
    fun stopByClipboardEntry(clipboardEntry: ClipboardEntry)
    suspend fun onExecutionEvent(onEvent: suspend (ClipboardExecutionEvent) -> Unit)

    sealed class ClipboardExecutionEvent : Event() {
        data class EntryExecutionResultEvent(
            val clipboardEntry: ClipboardEntry,
            val status: ExecutionStatus
        ) : ClipboardExecutionEvent()

        data class PayloadExecutionResultEvent(
            val payload: ClipboardEntryPayload,
            val status: ExecutionStatus
        ) : ClipboardExecutionEvent()
    }

    enum class ExecutionStatus {
        SUCCESS,
        ERROR
    }
}