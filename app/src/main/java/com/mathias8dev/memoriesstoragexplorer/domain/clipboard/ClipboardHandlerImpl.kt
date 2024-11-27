package com.mathias8dev.memoriesstoragexplorer.domain.clipboard

import android.os.Environment
import androidx.compose.runtime.mutableStateListOf
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FileExistsAction
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntry
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntryPayload
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationProgress
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationsAndroidService.FileOperationsEvent
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationsClipboardExecutorAndroidService
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.SkippedOperation
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isSystemFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.time.LocalDateTime
import kotlin.coroutines.coroutineContext


@KoinViewModel
class ClipboardHandlerImpl : ClipboardHandler, ViewModel() {

    private val _events = MutableSharedFlow<ClipboardHandler.ClipboardEvent>()


    private val _selectedMedia = mutableStateListOf<MediaInfo>()
    override val selectedMedia: List<MediaInfo>
        get() = _selectedMedia

    private val _clipboard = mutableStateListOf<ClipboardEntry>()
    override val clipboard: List<ClipboardEntry>
        get() = _clipboard


    override val fileOperationsProgress: Map<String, FileOperationProgress>?
        get() = fileOperationsService?.fileOperationsProgress

    override val skippedOperations: List<SkippedOperation>?
        get() = fileOperationsService?.skippedOperations


    private var fileOperationsService: FileOperationsClipboardExecutorAndroidService? = null


    fun setFileOperationsAndroidService(service: FileOperationsClipboardExecutorAndroidService?) {
        fileOperationsService = service
        Timber.d("bindToFileOperationsAndroidService: $service")
    }


    override fun onSelectedMediaRenamed(updatedName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val selectedList = selectedMedia.toList()
            val failedList = mutableListOf<MediaInfo>()
            _events.emit(ClipboardHandler.ClipboardEvent.StartSelectedMediaRenamed(selectedList))
            selectedMedia.forEach { mediaInfo ->
                Timber.d("onRenameSelectedMedia: $mediaInfo")
                mediaInfo.privateContentUri?.toFile()?.absolutePath?.let { filePath ->
                    kotlin.runCatching {
                        fileOperationsService?.renameFileSync(
                            filePath,
                            updatedName
                        )
                    }.onFailure {
                        failedList.add(mediaInfo)
                        Timber.d("onRenameSelectedMedia: $mediaInfo")
                        Timber.e(it)
                    }
                }
            }

            _selectedMedia.clear()
            _events.emit(ClipboardHandler.ClipboardEvent.EndSelectedMediaRenamed(selectedList, failedList))
        }
    }

    override fun stopExecutionByUids(vararg uids: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fileOperationsService?.stopByUids(*uids)
        }
    }

    override fun pauseExecutionByUids(vararg uids: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fileOperationsService?.pauseByUids(*uids)
        }
    }

    override fun resumeExecutionByUids(vararg uids: String) {
        viewModelScope.launch(Dispatchers.IO) {
            fileOperationsService?.resumeByUids(*uids)
        }
    }

    override fun getByUidOrNull(uid: String): Any? {
        return clipboard.firstOrNull { it.uid == uid } ?: clipboard.flatMap { it.payloads }.firstOrNull { it.uid == uid }
    }

    override fun resolveSkippedOperation(uid: String, updatedAction: FileExistsAction, remembered: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _events.emit(ClipboardHandler.ClipboardEvent.StartResolveSkippedOperations)
            fileOperationsService?.resolveSkippedOperationSync(uid, updatedAction, remembered)
            if (fileOperationsService?.skippedOperations?.isEmpty() == true && fileOperationsService?.fileOperationsProgress?.isEmpty() == true) {
                _events.emit(ClipboardHandler.ClipboardEvent.EndResolveSkippedOperations)
            }
        }
    }

    override suspend fun onFileOperationEvent(onEvent: suspend (FileOperationsEvent) -> Unit) {
        Timber.d("onFileOperationEvent: $onEvent")
        while (fileOperationsService == null) {
            Timber.d("onFileOperationEvent: Waiting for fileOperationsService")
            kotlinx.coroutines.delay(1000)
        }
        fileOperationsService?.onFileOperationEvent(onEvent)
    }

    override fun onRename() {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedMedia.find { it.privateContentUri?.toFile()?.isSystemFile() == true } != null) {
                _events.emit(ClipboardHandler.ClipboardEvent.ActionImpossible)
                return@launch
            }
            _events.emit(ClipboardHandler.ClipboardEvent.StartRename(_selectedMedia.first()))
        }
    }

    override fun onClearClipboardByUid(uid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _events.emit(ClipboardHandler.ClipboardEvent.StartClearClipboardByUid)
            val uids = clipboard.filter { it.uid == uid }.flatMap { it.payloads }.map { it.uid }.toTypedArray()
            fileOperationsService?.stopByUids(uid, *uids)
            _clipboard.removeAll { it.uid == uid }
            _events.emit(ClipboardHandler.ClipboardEvent.EndClearClipboardByUid)
        }
    }

    override fun onClearAllClipboardEntry() {
        viewModelScope.launch(Dispatchers.IO) {
            _events.emit(ClipboardHandler.ClipboardEvent.StartClearAllClipboardEntry)
            val uids = (clipboard.flatMap { it.payloads }.map { it.uid } + clipboard.map { it.uid }).toTypedArray()
            fileOperationsService?.stopByUids(*uids)
            _clipboard.clear()
            _events.emit(ClipboardHandler.ClipboardEvent.EndClearAllClipboardEntry)

        }
    }

    override fun onCutToClipboard() {
        viewModelScope.launch(Dispatchers.IO) scope@{
            if (selectedMedia.find { it.privateContentUri?.toFile()?.isSystemFile() == true } != null) {
                _events.emit(ClipboardHandler.ClipboardEvent.CutActionImpossible)
                return@scope
            }
            _events.emit(ClipboardHandler.ClipboardEvent.StartCutToClipboard(_selectedMedia.size))
            val now = LocalDateTime.now()
            _clipboard.add(
                ClipboardEntry(
                    intent = ClipboardEntry.Intent.CUT,
                    time = now,
                    payloads = selectedMedia.map { ClipboardEntryPayload(mediaInfo = it) }
                )
            )
            _events.emit(ClipboardHandler.ClipboardEvent.EndCutToClipboard(_selectedMedia.size))
            _selectedMedia.clear()

        }
    }

    override fun onCopyToClipboard() {
        viewModelScope.launch(Dispatchers.IO) {
            _events.emit(ClipboardHandler.ClipboardEvent.StartCopyToClipboard(_selectedMedia.size))
            val now = LocalDateTime.now()
            _clipboard.add(
                ClipboardEntry(
                    intent = ClipboardEntry.Intent.COPY,
                    time = now,
                    payloads = selectedMedia.map { ClipboardEntryPayload(mediaInfo = it) }
                )
            )
            _events.emit(ClipboardHandler.ClipboardEvent.EndCopyToClipboard(_selectedMedia.size))
            _selectedMedia.clear()
        }
    }

    override fun onDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            if (selectedMedia.find { it.privateContentUri?.toFile()?.isSystemFile() == true } != null) {
                _events.emit(ClipboardHandler.ClipboardEvent.ActionImpossible)
                return@launch
            }
            _events.emit(ClipboardHandler.ClipboardEvent.StartDelete(_selectedMedia.size))
            val copied = _selectedMedia.toList()
            _selectedMedia.clear()
            copied.map {
                async {
                    kotlin.runCatching {
                        it.privateContentUri?.toFile()?.let { file ->
                            if (file.isDirectory) fileOperationsService?.deleteDirectorySync(file.absolutePath)
                            else fileOperationsService?.silentDeleteFileSync(file.absolutePath)
                        }
                    }
                }
            }.awaitAll()
            _events.emit(ClipboardHandler.ClipboardEvent.EndDelete(copied.size))
        }
    }

    override fun onAddMedia(media: MediaInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_selectedMedia.find { it.privateContentUri == media.privateContentUri } == null) {
                _selectedMedia.add(media)
            }
        }
    }

    override fun onRemoveSelectedMedia(media: MediaInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedMedia.removeIf { it.privateContentUri == media.privateContentUri }
        }
    }

    override fun onAddAllMedia(list: List<MediaInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (_selectedMedia.isEmpty()) _selectedMedia.addAll(list)
            else if (selectedMedia.size < list.size) _selectedMedia.addAll(
                list.filter { !selectedMedia.contains(it) }
            ) else _selectedMedia.clear()
        }
    }

    override fun onRemoveAllSelectedMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            _events.emit(ClipboardHandler.ClipboardEvent.StartRemoveAllSelectedMedia)
            _selectedMedia.clear()
            _events.emit(ClipboardHandler.ClipboardEvent.EndRemoveAllSelectedMedia)
        }
    }

    override fun onClipboardEntryClick(entry: ClipboardEntry, destination: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _events.emit(ClipboardHandler.ClipboardEvent.StartClipboardEntryClick(entry))
            val rootPath = Environment.getExternalStorageDirectory().absolutePath
            if (destination == null || !destination.startsWith(rootPath)) {
                _events.emit(ClipboardHandler.ClipboardEvent.ActionImpossibleClipboardEntryClick(entry))
                return@launch
            }
            Timber.d("onClipboardEntryClick: $entry")
            Timber.d("onClipboardEntryClick: The destination is $destination")
            Timber.d("The fileOperationsService is $fileOperationsService")
            _clipboard.indexOfFirst { it.uid == entry.uid }.takeIf { it != -1 }?.let {
                _clipboard.set(it, entry.copy(status = ClipboardEntry.Status.STARTED))
            }
            val isSuccess = fileOperationsService?.executeSync(entry, destination) ?: false
            _clipboard.removeIf { it.uid == entry.uid }
            Timber.d("onClipboardEntryClick: executeSync")
            if (isSuccess) _events.emit(ClipboardHandler.ClipboardEvent.EndClipboardEntryClick(entry))
        }
    }

    override fun onClipboardEntryPayloadClick(entry: ClipboardEntry, payload: ClipboardEntryPayload, destination: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _events.emit(ClipboardHandler.ClipboardEvent.StartClipboardEntryPayloadClick(payload))
            val rootPath = Environment.getExternalStorageDirectory().absolutePath
            if (destination == null || !destination.startsWith(rootPath)) {
                _events.emit(ClipboardHandler.ClipboardEvent.ActionImpossibleClipboardEntryPayloadClick(payload))
                return@launch
            }

            _clipboard.indexOfFirst { it.uid == entry.uid }.takeIf { it != -1 }?.let { clipboardEntryIndex ->
                _clipboard[clipboardEntryIndex].payloads.indexOfFirst { clipboardPayload -> clipboardPayload.uid == payload.uid }.takeIf { payloadIndex -> payloadIndex != -1 }?.let { payloadIndex ->
                    _clipboard.set(clipboardEntryIndex, entry.copy(payloads = entry.payloads.toMutableList().apply { set(payloadIndex, payload.copy(status = ClipboardEntryPayload.Status.STARTED)) }))
                }
            }


            fileOperationsService?.executeSync(payload, entry.intent, destination)


            // Remove the payload from the clipboard entry
            launch {
                _clipboard.indexOfFirst { it.uid == entry.uid }.takeIf { it != -1 }?.let { clipboardEntryIndex ->
                    _clipboard[clipboardEntryIndex] = entry.copy(payloads = entry.payloads.filter { it.uid != payload.uid })
                }
            }

            _events.emit(ClipboardHandler.ClipboardEvent.EndClipboardEntryPayloadClick(payload))
        }
    }

    override suspend fun listen(onEvent: (ClipboardHandler.ClipboardEvent) -> Unit) {
        _events.collectLatest { event ->
            coroutineContext.ensureActive()
            onEvent(event)
        }
    }
}