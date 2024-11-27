package com.mathias8dev.memoriesstoragexplorer.domain.clipboard

import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FileExistsAction
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntry
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntryPayload
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationProgress
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationsAndroidService
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.SkippedOperation

interface ClipboardHandler {

    val selectedMedia: List<MediaInfo>
    val clipboard: List<ClipboardEntry>

    val fileOperationsProgress: Map<String, FileOperationProgress>?
    val skippedOperations: List<SkippedOperation>?

    fun onAddMedia(media: MediaInfo)
    fun onRemoveSelectedMedia(media: MediaInfo)
    fun onAddAllMedia(list: List<MediaInfo>)
    fun onRemoveAllSelectedMedia()
    fun onDelete()
    fun onCopyToClipboard()
    fun onCutToClipboard()
    fun onRename()
    fun onClearClipboardByUid(uid: String)
    fun onClearAllClipboardEntry()
    fun onClipboardEntryClick(entry: ClipboardEntry, destination: String?)
    fun onClipboardEntryPayloadClick(entry: ClipboardEntry, payload: ClipboardEntryPayload, destination: String?)
    fun onSelectedMediaRenamed(updatedName: String)
    fun stopExecutionByUids(vararg uids: String)
    fun pauseExecutionByUids(vararg uids: String)
    fun resumeExecutionByUids(vararg uids: String)
    fun getByUidOrNull(uid: String): Any?
    fun resolveSkippedOperation(uid: String, updatedAction: FileExistsAction, remembered: Boolean)
    suspend fun onFileOperationEvent(onEvent: suspend (FileOperationsAndroidService.FileOperationsEvent) -> Unit)


    suspend fun listen(onEvent: (ClipboardEvent) -> Unit)

    sealed class ClipboardEvent {
        data object StartRemoveAllSelectedMedia : ClipboardEvent()
        data object EndRemoveAllSelectedMedia : ClipboardEvent()
        data class StartDelete(val size: Int) : ClipboardEvent()
        data class EndDelete(val size: Int) : ClipboardEvent()
        data class StartCopyToClipboard(val size: Int) : ClipboardEvent()
        data class EndCopyToClipboard(val size: Int) : ClipboardEvent()
        data class StartCutToClipboard(val size: Int) : ClipboardEvent()
        data class EndCutToClipboard(val size: Int) : ClipboardEvent()
        data object StartClearClipboardByUid : ClipboardEvent()
        data object EndClearClipboardByUid : ClipboardEvent()
        data object StartClearAllClipboardEntry : ClipboardEvent()
        data object EndClearAllClipboardEntry : ClipboardEvent()
        data object StartResolveSkippedOperations : ClipboardEvent()
        data object EndResolveSkippedOperations : ClipboardEvent()
        data class StartRename(val mediaInfo: MediaInfo) : ClipboardEvent()
        data class StartSelectedMediaRenamed(val selected: List<MediaInfo>) : ClipboardEvent()
        data class ErrorSelectedMediaRenamed(val selected: List<MediaInfo>) : ClipboardEvent()
        data class EndSelectedMediaRenamed(val selected: List<MediaInfo>, val failed: List<MediaInfo>) : ClipboardEvent()
        data class StartClipboardEntryClick(val entry: ClipboardEntry) : ClipboardEvent()
        data class ActionImpossibleClipboardEntryClick(val entry: ClipboardEntry) : ClipboardEvent()
        data class EndClipboardEntryClick(val entry: ClipboardEntry) : ClipboardEvent()
        data class StartClipboardEntryPayloadClick(val payload: ClipboardEntryPayload) : ClipboardEvent()
        data class ActionImpossibleClipboardEntryPayloadClick(val payload: ClipboardEntryPayload) : ClipboardEvent()
        data class EndClipboardEntryPayloadClick(val payload: ClipboardEntryPayload) : ClipboardEvent()
        data object CutActionImpossible : ClipboardEvent()
        data object ActionImpossible : ClipboardEvent()
    }

}