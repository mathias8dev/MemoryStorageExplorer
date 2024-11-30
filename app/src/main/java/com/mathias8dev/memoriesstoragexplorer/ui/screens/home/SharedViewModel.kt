package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.FilterQuery
import com.mathias8dev.memoriesstoragexplorer.domain.enums.AddMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.SortMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.LoadStringResourceUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.CurrentPathIsStorageVolumePathUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.GetFileNameUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.GetSelectedDiskOverviewUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations.FileCreateDirectoryUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations.FileCreateUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryAllApksUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryAllArchivesUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryAllAudiosUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryAllDocumentsUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryAllFromRecycleBinUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryAllImagesUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryAllMediasUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryAllVideosUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryInstalledAppsUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryMediaListFromPathUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryRecentFilesUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.utils.Resource
import com.mathias8dev.memoriesstoragexplorer.domain.utils.data
import com.mathias8dev.memoriesstoragexplorer.domain.utils.isSuccess
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise
import com.mathias8dev.memoriesstoragexplorer.ui.activities.imageViewer.ImageViewerActivity
import com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaPlayer.MediaPlayerActivity
import com.mathias8dev.memoriesstoragexplorer.ui.activities.pdfViewer.PdfViewerActivity
import com.mathias8dev.memoriesstoragexplorer.ui.composables.AutoGrowTabController
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaGroup
import com.mathias8dev.memoriesstoragexplorer.ui.composables.SelectedDiskOverview
import com.mathias8dev.memoriesstoragexplorer.ui.composables.SelectedPathView
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asContentSchemeUri
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asSelectedPathView
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isPdfDocument
import com.mathias8dev.memoriesstoragexplorer.ui.utils.mimeData
import de.datlag.mimemagic.MimeData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@KoinViewModel
class SharedViewModel(
    private val queryMediaListFromPathUseCase: QueryMediaListFromPathUseCase,
    private val queryAllAudiosUseCase: QueryAllAudiosUseCase,
    private val queryAllVideosUseCase: QueryAllVideosUseCase,
    private val queryAllDocumentsUseCase: QueryAllDocumentsUseCase,
    private val queryAllImagesUseCase: QueryAllImagesUseCase,
    private val queryAllArchivesUseCase: QueryAllArchivesUseCase,
    private val queryRecentFilesUseCase: QueryRecentFilesUseCase,
    private val queryAllMediaUseCase: QueryAllMediasUseCase,
    private val queryAllApksUseCase: QueryAllApksUseCase,
    private val queryAllFromRecycleBinUseCase: QueryAllFromRecycleBinUseCase,
    private val queryInstalledApps: QueryInstalledAppsUseCase,
    private val getSelectedDiskOverviewUseCase: GetSelectedDiskOverviewUseCase,
    private val getFileNameUseCase: GetFileNameUseCase,
    private val currentPathIsStorageVolumePathUseCase: CurrentPathIsStorageVolumePathUseCase,
    private val loadStringResourceUseCase: LoadStringResourceUseCase,
    private val createFileUseCase: FileCreateUseCase,
    private val createDirectoryUseCase: FileCreateDirectoryUseCase
) : ViewModel() {

    private var cachedRootMediaInfo: List<MediaInfo>? = null

    private val defaultStorageRootPath = Environment.getExternalStorageDirectory().absolutePath

    private val _mediaQueryRequestResponse by lazy { MutableStateFlow<Resource<List<MediaInfo>>>(Resource.Loading()) }

    val mediaQueryRequestResponse by lazy { _mediaQueryRequestResponse.asStateFlow() }

    private val backStackHolder = BackStackHolder()
    private val tabsController = AutoGrowTabController()

    private val _backStackEntry = MutableStateFlow<BackStackEntry?>(null)
    val backStackEntry = _backStackEntry.asStateFlow()

    var isCurrentStackEmpty = mutableStateOf(false)
        private set

    var currentStackSize = mutableIntStateOf(0)
        private set

    val tabs = tabsController.tabs
    val tabIndex = tabsController.tabIndex

    private var previousPage = 0
    val pagerState = PagerState(pageCount = { Short.MAX_VALUE.toInt() })


    private val _effect = MutableSharedFlow<Effect>()
    val effect = _effect.asSharedFlow()


    var selectedDiskOverview by mutableStateOf<SelectedDiskOverview?>(null)
        private set
    var currentPathBaseStat by mutableStateOf<SelectedPathView?>(null)
        private set
    var currentRootPath by mutableStateOf<String>(defaultStorageRootPath)
        private set


    init {
        viewModelScope.launch {
            launch {
                snapshotFlow { pagerState.currentPage }.collect {
                    if (previousPage < it) onTabIndexChangedBasedOnSwipe(false)
                    else onTabIndexChangedBasedOnSwipe(true)

                    previousPage = it
                }
            }

        }
    }


    fun onBackStackEntryChanged(position: Int = tabIndex.value, entry: BackStackEntry) {
        viewModelScope.launch {
            Timber.d("onBackStackEntryChanged: ${tabIndex.value} - $entry")
            backStackHolder.setCurrentPosition(position)
            backStackHolder.addOrUpdateAt(entry = entry)
            Timber.d("backStackEntry: ${backStackEntry.value}")
            onReload(wipeCache = false, force = true)
        }

    }

    fun onTabIndexChanged(index: Int) {
        viewModelScope.launch {
            Timber.d("onTabIndexChanged: $index")
            tabsController.updateTabIndex(index)
            backStackHolder.setCurrentPosition(index)
            onReload(wipeCache = false, force = true)
            Timber.d("backStackEntry: ${backStackEntry.value}")
        }
    }

    fun onTabRemovedAt(index: Int) {
        viewModelScope.launch {
            Timber.d("onTabRemovedAt: $index")
            tabsController.removeTabAt(index)
            backStackHolder.removeAt(index)
            backStackHolder.setCurrentPosition(tabIndex.value)
            onReload(wipeCache = false, force = true)
            Timber.d("backStackEntry: ${backStackEntry.value}")
        }
    }

    private fun onTabIndexChangedBasedOnSwipe(isSwipeToLeft: Boolean = false) {
        viewModelScope.launch {
            Timber.d("onTabIndexChangedBasedOnSwipe")
            tabsController.updateTabIndexBasedOnSwipe(isSwipeToLeft)
            val entry = backStackHolder.getAt(tabIndex.value) ?: BackStackHolder.default
            onBackStackEntryChanged(entry = entry)
            Timber.d("backStackEntry: ${backStackEntry.value}")
        }
    }

    fun onPopCurrentBackStack() {
        viewModelScope.launch {
            Timber.d("onPopCurrentBackStack")
            backStackHolder.removeAt()
            onReload(wipeCache = false, force = true)
            Timber.d("backStackEntry: ${backStackEntry.value}")
        }
    }


    private fun updateStates() {
        Timber.d("updateStates")
        viewModelScope.launch {
            updateBackStackState()
            updateRootPath()
            updateSelectedPathView()
            updateSelectedDiskOverview()
            updateTabName()
        }
    }

    private fun updateRootPath() {
        currentRootPath = _backStackEntry.value?.path.otherwise(defaultStorageRootPath)
    }

    private fun updateSelectedPathView() {
        viewModelScope.launch {
            MediaGroup.fromPath(currentRootPath).takeIf { it != MediaGroup.InternalStorage }?.let { mediaGroup ->
                delay(200)
                while (_mediaQueryRequestResponse.value !is Resource.Success) {
                    delay(300)
                }
                Timber.d("The media group is $mediaGroup")
                currentPathBaseStat = SelectedPathView(
                    path = currentRootPath,
                    name = mediaGroup.title,
                    filesCount = if (mediaGroup == MediaGroup.Home) null else _mediaQueryRequestResponse.value.data?.size,
                    foldersCount = if (mediaGroup == MediaGroup.Home) MediaGroup.homeList.size else null
                )
                Timber.d("SelectedPathView: $currentPathBaseStat")
            }.otherwise {
                File(currentRootPath).asSelectedPathView()?.let {
                    currentPathBaseStat = it
                }
            }
        }
    }

    private fun updateSelectedDiskOverview() {
        viewModelScope.launch {
            Timber.d("updateSelectedDiskOverview")
            Timber.d("selectedDiskOverview == null: ${selectedDiskOverview == null}")
            Timber.d("!currentPathIsStorageVolumePath(currentRootPath): ${!currentPathIsStorageVolumePathUseCase(currentRootPath)}")
            MediaGroup.fromPath(currentRootPath).takeIf { it != MediaGroup.InternalStorage }?.let { mediaGroup ->
                Timber.d("updateSelectedDiskOverview: $mediaGroup")
                selectedDiskOverview = null
            }.otherwise {
                val currentPathIsNotStorageVolumePath = !currentPathIsStorageVolumePathUseCase(currentRootPath)
                val lastPoppedPath = backStackHolder.lastPoppedAt()?.path
                val lastSeenPath = if (currentPathIsNotStorageVolumePath) currentRootPath else lastPoppedPath
                if (selectedDiskOverview == null || currentPathIsNotStorageVolumePath) {
                    val overview = getSelectedDiskOverviewUseCase.invoke(
                        currentPath = currentRootPath,
                        lastSeenPath = lastSeenPath
                    )
                    selectedDiskOverview = overview
                    Timber.d("SelectedDiskOverview: $overview")
                }
            }

        }
    }

    private fun updateBackStackState() {
        currentStackSize.intValue = backStackHolder.stackSizeAt()
        isCurrentStackEmpty.value = backStackHolder.isStackEmptyAt()
        _backStackEntry.value = backStackHolder.getAt().otherwise(BackStackHolder.default)
    }

    private fun updateTabName() {
        viewModelScope.launch {
            val name = MediaGroup.fromPath(currentRootPath).takeIf { it != MediaGroup.InternalStorage }?.title.otherwise(getFileNameUseCase(currentRootPath))
            tabsController.updateTabNameAt(title = name)
        }
    }

    suspend fun listRootFiles(): List<String> = suspendCoroutine { continuation ->

        val fileList = mutableListOf<String>()
        try {
            Timber.d("Calling ls /")
            val process = Runtime.getRuntime().exec("ls /")
            val reader = process.inputStream.bufferedReader()
            reader.useLines { lines ->
                lines.forEach {
                    fileList.add(it)
                    Timber.d("Adding line to fileList $it")
                }
                continuation.resume(fileList)
            }
        } catch (e: Exception) {
            Timber.d("Error occurred: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun queryFilesByAbstractPath(
        path: String,
        filterQueries: List<FilterQuery> = emptyList(),
        sortMode: SortMode = SortMode.NAME_AZ
    ) {
        viewModelScope.launch {
            val queryFunc: (suspend () -> List<MediaInfo>)? = when {
                path == MediaGroup.Audio.path -> queryAllAudiosUseCase::invoke
                path == MediaGroup.Video.path -> queryAllVideosUseCase::invoke
                path == MediaGroup.Document.path -> queryAllDocumentsUseCase::invoke
                path == MediaGroup.Image.path -> queryAllImagesUseCase::invoke
                path == MediaGroup.Archive.path -> queryAllArchivesUseCase::invoke
                path == MediaGroup.RecentFiles.path -> queryRecentFilesUseCase::invoke
                path == MediaGroup.AllFiles.path -> queryAllMediaUseCase::invoke
                path == MediaGroup.Apk.path -> queryAllApksUseCase::invoke
                path == MediaGroup.RecycleBin.path -> queryAllFromRecycleBinUseCase::invoke
                path == MediaGroup.App.path -> queryInstalledApps::invoke
                path == MediaGroup.Root.path -> {
                    Timber.d("Listing root files")
                    kotlin.runCatching {
                        val files = listRootFiles()
                        Timber.d("Root files: $files")
                    }
                    suspend { emptyList() }
                }

                path.startsWith(defaultStorageRootPath) -> suspend { queryMediaListFromPathUseCase.invoke(path) }


                else -> null
            }

            queryFunc?.let { func ->
                _mediaQueryRequestResponse.emit(Resource.Loading())
                val result = if (!cachedRootMediaInfo.isNullOrEmpty() && path == defaultStorageRootPath) cachedRootMediaInfo else kotlin.runCatching { func.invoke() }.getOrElse {
                    _mediaQueryRequestResponse.emit(Resource.Error())
                    null
                }
                result?.let {
                    val filtered = it.filter { mediaInfo -> filterQueries.all { filterQuery -> filterQuery.filter(mediaInfo, false) } }
                    val sorted = filtered.sortedWith(sortMode)
                    _mediaQueryRequestResponse.emit(Resource.Success(sorted))
                    if (cachedRootMediaInfo.isNullOrEmpty() && path == defaultStorageRootPath) cachedRootMediaInfo = sorted
                }
            }

        }
    }


    fun onMediaGroupClick(
        mediaGroup: MediaGroup
    ) {
        Timber.d("onMediaGroupClick: $mediaGroup")
        Timber.d("The current path is $currentRootPath")
        if (currentRootPath == mediaGroup.path) return
        onBackStackEntryChanged(
            entry = BackStackEntry(
                path = mediaGroup.path,
            )
        )
    }

    fun onMediaClick(
        mediaInfo: MediaInfo,
        context: Context,
    ) {
        viewModelScope.launch {
            val derivedFile = mediaInfo.privateContentUri!!.toFile()
            val mimeData = derivedFile.mimeData
            if (derivedFile.isFile) {
                when {
                    mimeData?.isImage == true -> {
                        launchViewerIntent(
                            context,
                            derivedFile,
                            mimeData.mimeType,
                            ImageViewerActivity::class.java
                        )
                    }

                    mimeData?.isAudio == true -> {
                        launchViewerIntent(
                            context,
                            derivedFile,
                            mimeData.mimeType,
                            MediaPlayerActivity::class.java
                        )

                    }

                    mimeData?.isVideo == true -> {
                        launchViewerIntent(
                            context,
                            derivedFile,
                            mimeData.mimeType,
                            MediaPlayerActivity::class.java
                        )
                    }

                    mimeData?.isDocument == true && derivedFile.extension.isPdfDocument() -> {
                        launchViewerIntent(
                            context,
                            derivedFile,
                            mimeData.mimeType,
                            PdfViewerActivity::class.java
                        )
                    }

                    else -> {
                        launchIntent(
                            uri = mediaInfo.contentUri ?: derivedFile.asContentSchemeUri(context),
                            context = context,
                            mimeType = mimeData?.mimeType
                        )
                    }
                }
            } else {
                onBackStackEntryChanged(
                    entry = BackStackEntry(
                        path = derivedFile.absolutePath,
                    )
                )
            }
        }
    }

    private fun <T> launchViewerIntent(
        context: Context,
        file: File,
        mimeType: String?,
        viewerActivity: Class<T>
    ) {
        viewModelScope.launch {
            val type = mimeType.otherwise(kotlin.runCatching { MimeData.fromFile(file) }.getOrNull()?.mimeType)
            val intent = Intent()
            intent.setClass(context, viewerActivity)
            val uri = file.asContentSchemeUri(context)
            intent.setDataAndType(uri, type)
            context.startActivity(intent)
        }
    }

    fun onReload(wipeCache: Boolean = true, force: Boolean = true) {
        Timber.d("onReload")
        viewModelScope.launch {
            if (wipeCache) cachedRootMediaInfo = null
            updateStates()
            loadContentByLastBackStackEntry(force)
        }
    }

    fun onFilter(queries: List<FilterQuery>) {
        viewModelScope.launch {
            val entry = (backStackEntry.value ?: BackStackHolder.default).copy(filterQueries = queries)
            backStackHolder.updateLastEntryAt(entry = entry)
            onReload(wipeCache = false, force = false)
        }
    }

    fun onLayout(mode: LayoutMode) {
        viewModelScope.launch {

        }
    }

    fun onSort(sortMode: SortMode) {
        viewModelScope.launch {
            val entry = (backStackEntry.value ?: BackStackHolder.default).copy(sortMode = sortMode)
            backStackHolder.updateLastEntryAt(entry = entry)
            onReload(wipeCache = false, force = false)
        }
    }

    fun onAdd(addMode: AddMode) {
        viewModelScope.launch {
            when (addMode) {
                AddMode.FILE -> onAddFile()
                AddMode.FOLDER -> onAddFolder()
            }
        }
    }

    private fun onAddFile() {
        viewModelScope.launch {
            _effect.emit(
                Effect.ShowAddFileDialog
            )
        }
    }

    private fun onAddFolder() {
        viewModelScope.launch {
            _effect.emit(
                Effect.ShowAddFolderDialog
            )
        }
    }


    private fun loadContentByLastBackStackEntry(force: Boolean = true) {
        viewModelScope.launch {
            val entry = backStackHolder.getAt().otherwise(BackStackHolder.default)
            if (_mediaQueryRequestResponse.value.isSuccess() && !force) {
                _mediaQueryRequestResponse.value.data?.let { mediaList ->
                    val filtered = mediaList.filter { mediaInfo -> entry.filterQueries.all { filterQuery -> filterQuery.filter(mediaInfo, false) } }
                    val sorted = filtered.sortedWith(entry.sortMode)
                    _mediaQueryRequestResponse.emit(Resource.Success(sorted))

                    return@launch
                }

            }

            queryFilesByAbstractPath(
                path = entry.path ?: defaultStorageRootPath,
                filterQueries = entry.filterQueries,
                sortMode = entry.sortMode
            )

        }
    }


    fun onAddFolder(folderName: String, count: Int) {
        viewModelScope.launch {
            if (count > 0) {
                _effect.emit(Effect.ShowFullscreenLoader)
                for (i in 0 until count) {
                    kotlin.runCatching {
                        val file = File(currentRootPath, folderName)
                        createDirectoryUseCase(file.absolutePath)
                    }
                }
                onReload(true)
                _effect.emit(Effect.HideAddMediaDialog)
                emitSnackbarEffect(
                    loadStringResourceUseCase.invoke(R.plurals.folder_created_successfully, count)
                )

                _effect.emit(Effect.HideFullscreenLoader)
            }
        }
    }

    fun onAddFile(fileName: String, extension: String, count: Int) {
        viewModelScope.launch {
            if (count > 0) {
                _effect.emit(Effect.ShowFullscreenLoader)
                for (i in 0 until count) {
                    kotlin.runCatching {
                        createFileUseCase(currentRootPath, fileName, extension)
                    }
                }
                onReload(true)
                _effect.emit(Effect.HideAddMediaDialog)
                emitSnackbarEffect(
                    loadStringResourceUseCase.invoke(R.plurals.file_created_successfully, count),
                )

                _effect.emit(Effect.HideFullscreenLoader)
            }
        }
    }

    private fun emitToastEffect(message: String, duration: Int = Toast.LENGTH_SHORT) {
        viewModelScope.launch {
            _effect.emit(
                Effect.ShowToast(
                    message = message,
                    duration = duration
                )
            )
        }
    }

    private fun emitSnackbarEffect(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        viewModelScope.launch {
            _effect.emit(
                Effect.ShowSnackBar(
                    message = message,
                    duration = duration
                )
            )
        }
    }

    sealed class Effect {
        data class ShowToast(
            val message: String,
            val duration: Int
        ) : Effect()

        data class ShowSnackBar(
            val message: String,
            val duration: SnackbarDuration
        ) : Effect()


        data object HideFullscreenLoader : Effect()
        data object ShowFullscreenLoader : Effect()
        data object ShowAddFileDialog : Effect()
        data object ShowAddFolderDialog : Effect()
        data object HideAddMediaDialog : Effect()
    }
}


fun launchIntent(uri: Uri?, action: String = Intent.ACTION_VIEW, mimeType: String?, context: Context) {
    val intent = Intent()
    intent.action = action
    intent.setDataAndType(uri, mimeType)
    val chooserIntent = Intent.createChooser(intent, context.resources.getString((R.string.please_select_an_app)))
    context.startActivity(chooserIntent)
}

fun launchSendIntent(uris: Collection<Uri>, context: Context) {
    if (uris.isEmpty()) return
    // Group the URIs by MIME type
    val uriMap = uris.groupBy { context.contentResolver.getType(it) }

    uriMap.forEach { (mimeType, urisOfType) ->
        val action = if (urisOfType.size == 1) Intent.ACTION_SEND else Intent.ACTION_SEND_MULTIPLE

        val intent = Intent(action).apply {
            type = mimeType
            if (urisOfType.size == 1) {
                putExtra(Intent.EXTRA_STREAM, urisOfType.first())
            } else {
                putExtra(Intent.EXTRA_STREAM, ArrayList(urisOfType))
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        // Launch the chooser for each MIME type group
        val chooserIntent = Intent.createChooser(intent, context.getString(R.string.please_select_an_app))
        context.startActivity(chooserIntent)
    }
}