package com.mathias8dev.memoriesstoragexplorer.ui.screens.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
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
import com.mathias8dev.memoriesstoragexplorer.data.repositories.AppSettingsRepository
import com.mathias8dev.memoriesstoragexplorer.domain.FilterQuery
import com.mathias8dev.memoriesstoragexplorer.domain.cache.MediaCacheManager
import com.mathias8dev.memoriesstoragexplorer.domain.enums.AddMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.enums.SortMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.services.AppSettingsService
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.LoadStringResourceUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.CurrentPathIsStorageVolumePathUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.GetFileNameUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.GetStorageVolumeOverviewUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.StorageVolumeEvent
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.StorageVolumeMonitorUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.StorageVolumeOverview
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations.FileCreateDirectoryUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.fileOperations.FileCreateUseCase
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.monitor.FileChangeEvent
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.monitor.FileChangeMonitorUseCase
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
import com.mathias8dev.memoriesstoragexplorer.domain.utils.otherwise
import com.mathias8dev.memoriesstoragexplorer.ui.activities.imageViewer.ImageViewerActivity
import com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaPlayer.MediaPlayerActivity
import com.mathias8dev.memoriesstoragexplorer.ui.activities.pdfViewer.PdfViewerActivity
import com.mathias8dev.memoriesstoragexplorer.ui.composables.SelectedPathView
import com.mathias8dev.memoriesstoragexplorer.ui.composables.autoGrow.AutoGrowTabController
import com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaGroup.MediaGroup
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
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration.Companion.seconds


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
    private val getStorageVolumeOverviewUseCase: GetStorageVolumeOverviewUseCase,
    private val getFileNameUseCase: GetFileNameUseCase,
    private val currentPathIsStorageVolumePathUseCase: CurrentPathIsStorageVolumePathUseCase,
    private val loadStringResourceUseCase: LoadStringResourceUseCase,
    private val createFileUseCase: FileCreateUseCase,
    private val createDirectoryUseCase: FileCreateDirectoryUseCase,
    private val storageVolumeMonitorUseCase: StorageVolumeMonitorUseCase,
    private val fileChangeMonitorUseCase: FileChangeMonitorUseCase,
    private val cacheManager: MediaCacheManager,
    private val appSettingsRepository: AppSettingsRepository,
) : ViewModel(), AppSettingsService by appSettingsRepository {

    private var cachedRootMediaInfo: List<MediaInfo>? = null

    private val defaultStorageRootPath = Environment.getExternalStorageDirectory().absolutePath

    private val _mediaQueryRequestResponse by lazy { MutableStateFlow<Resource<List<MediaInfo>>>(Resource.Loading()) }

    val mediaQueryRequestResponse by lazy { _mediaQueryRequestResponse.asStateFlow() }

    private val backStacksHolder = BackStacksHolder()
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

    private val _storageVolumesChanged = MutableSharedFlow<Unit>(replay = 1)
    val storageVolumesChanged = _storageVolumesChanged.asSharedFlow()

    var selectedVolumeOverview by mutableStateOf<StorageVolumeOverview?>(null)
        private set
    var currentPathBaseStat by mutableStateOf<SelectedPathView?>(null)
        private set
    var currentRootPath by mutableStateOf<String>(defaultStorageRootPath)
        private set

    private var initialized = false

    init {
        viewModelScope.launch {
            launch {
                snapshotFlow { pagerState.currentPage }.collect {
                    Timber.d("initial collect of pagerState")
                    if (initialized) {
                        if (previousPage < it) onTabIndexChangedBasedOnSwipe(false)
                        else onTabIndexChangedBasedOnSwipe(true)
                    }

                    previousPage = it
                }
            }

            // Listen for storage volume mount/unmount events
            launch {
                storageVolumeMonitorUseCase.events.collect { event ->
                    Timber.d("Storage event received: $event")
                    handleStorageVolumeEvent(event)
                }
            }

            // Listen for file system changes
            launch {
                fileChangeMonitorUseCase.events.collect { event ->
                    Timber.d("File change event received: $event")
                    handleFileChangeEvent(event)
                }
            }

            delay(1.seconds)
            initialized = true // We don't want the initialization of the pagerState trigger the collect. Since it can false the backstack

            // Emit initial storage volumes state
            _storageVolumesChanged.emit(Unit)
        }

    }


    fun onBackStackEntryChanged(position: Int = tabIndex.value, entry: BackStackEntry) {
        viewModelScope.launch {
            Timber.d("onBackStackEntryChanged: ${tabIndex.value} - $entry")
            backStacksHolder.setCurrentStackPosition(position)
            backStacksHolder.addEntryAt(entry = entry)
            Timber.d("backStackEntry: ${backStackEntry.value}")

            // Start monitoring directory if it's a file system path
            entry.path?.let { path ->
                if (!path.startsWith("media_group:")) {
                    // It's a real file system path, start monitoring
                    fileChangeMonitorUseCase.startDirectoryMonitoring(path)
                }
            }

            onReload(wipeCache = false, force = false)
        }


    }

    fun onTabIndexChanged(index: Int) {
        viewModelScope.launch {
            Timber.d("onTabIndexChanged: $index")
            tabsController.updateTabIndex(index)
            backStacksHolder.setCurrentStackPosition(index)
            onReload(wipeCache = false, force = false)
            Timber.d("backStackEntry: ${backStackEntry.value}")
        }
    }

    fun onTabRemovedAt(index: Int) {
        viewModelScope.launch {
            Timber.d("onTabRemovedAt: $index")
            tabsController.removeTabAt(index)
            backStacksHolder.deleteStackAndShiftAt(index)
            //backStacksHolder.setCurrentStackPosition(tabIndex.value)
            onReload(wipeCache = false, force = true)
            Timber.d("backStackEntry: ${backStackEntry.value}")
        }
    }

    private fun onTabIndexChangedBasedOnSwipe(isSwipeToLeft: Boolean = false) {
        viewModelScope.launch {
            Timber.d("onTabIndexChangedBasedOnSwipe")
            tabsController.updateTabIndexBasedOnSwipe(isSwipeToLeft)
            val entry = backStacksHolder.getEntryAt(tabIndex.value) ?: BackStackEntry()
            onBackStackEntryChanged(entry = entry)
            Timber.d("backStackEntry: ${backStackEntry.value}")
        }
    }

    fun onPopCurrentBackStack() {
        viewModelScope.launch {
            Timber.d("onPopCurrentBackStack")
            backStacksHolder.removeLastEntryAt()
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
            updateStorageOverview()
            updateTabName()
        }
    }

    private fun updateRootPath() {
        currentRootPath = _backStackEntry.value?.path.otherwise(defaultStorageRootPath)
    }

    private fun updateSelectedPathView() {
        viewModelScope.launch {
            MediaGroup.fromPath(currentRootPath)?.let { mediaGroup ->
                delay(200)
                while (_mediaQueryRequestResponse.value !is Resource.Success) {
                    delay(300)
                }
                Timber.d("The media group is $mediaGroup")
                currentPathBaseStat = SelectedPathView(
                    path = currentRootPath,
                    name = loadStringResourceUseCase(mediaGroup.titleRes!!),
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

    private fun updateStorageOverview() {
        viewModelScope.launch {
            Timber.d("updateStorageOverview")
            Timber.d("selectedDiskOverview == null: ${selectedVolumeOverview == null}")
            Timber.d("!currentPathIsStorageVolumePath(currentRootPath): ${!currentPathIsStorageVolumePathUseCase(currentRootPath)}")
            MediaGroup.fromPath(currentRootPath)?.takeIf { !currentPathIsStorageVolumePathUseCase.invoke(it.path) }?.let { mediaGroup ->
                Timber.d("updateStorageOverview: $mediaGroup")
                selectedVolumeOverview = null
            }.otherwise {
                val currentPathIsNotStorageVolumePath = !currentPathIsStorageVolumePathUseCase(currentRootPath)
                val lastPoppedPath = backStacksHolder.lastPoppedAt()?.path
                val lastSeenPath = if (currentPathIsNotStorageVolumePath) currentRootPath else lastPoppedPath
                if (selectedVolumeOverview == null || currentPathIsNotStorageVolumePath) {
                    val overview = getStorageVolumeOverviewUseCase.invoke(
                        currentPath = currentRootPath,
                        lastSeenPath = lastSeenPath
                    )
                    selectedVolumeOverview = overview
                    Timber.d("StorageOverview: $overview")
                }
            }

        }
    }

    private fun updateBackStackState() {
        if (backStacksHolder.isStackEmptyAt()) {
            onBackStackEntryChanged(entry = BackStackEntry())
            return
        }
        currentStackSize.intValue = backStacksHolder.stackSizeAt()
        isCurrentStackEmpty.value = backStacksHolder.isStackEmptyAt()
        _backStackEntry.value = backStacksHolder.getEntryAt()!!
    }

    private fun updateTabName() {
        viewModelScope.launch {
            val name = MediaGroup.fromPath(currentRootPath)
                ?.titleRes?.let { loadStringResourceUseCase(it) }
                .otherwise(getFileNameUseCase(currentRootPath))
            tabsController.updateTabNameAt(title = name)
        }
    }

    private fun handleStorageVolumeEvent(event: StorageVolumeEvent) {
        viewModelScope.launch {
            when (event) {
                is StorageVolumeEvent.Mounted -> {
                    Timber.i("Storage mounted at: ${event.path}")
                    // Show notification to user
                    event.path?.let { path ->
                        val volumeName = kotlin.runCatching {
                            getFileNameUseCase(path)
                        }.getOrElse { "External storage" }
                        emitSnackbarEffect(
                            loadStringResourceUseCase.invoke(R.string.storage_mounted, 1, volumeName)
                        )
                    }
                    // Refresh storage overview if we're currently viewing storage volumes
                    if (currentPathIsStorageVolumePathUseCase(currentRootPath)) {
                        updateStorageOverview()
                    }
                    // Notify UI that storage volumes list has changed
                    _storageVolumesChanged.emit(Unit)
                }

                is StorageVolumeEvent.Unmounted,
                is StorageVolumeEvent.Ejected,
                is StorageVolumeEvent.Removed -> {
                    val action = when (event) {
                        is StorageVolumeEvent.Unmounted -> "unmounted"
                        is StorageVolumeEvent.Ejected -> "ejected"
                        is StorageVolumeEvent.Removed -> "removed"
                        else -> "disconnected"
                    }
                    val path = when (event) {
                        is StorageVolumeEvent.Unmounted -> event.path
                        is StorageVolumeEvent.Ejected -> event.path
                        is StorageVolumeEvent.Removed -> event.path
                        else -> null
                    }
                    Timber.i("Storage $action at: $path")

                    // Check if the current path is affected by this unmount
                    val isCurrentPathAffected = path != null && currentRootPath.startsWith(path, ignoreCase = true)

                    if (isCurrentPathAffected) {
                        // Navigate back to default storage if the current path is on the unmounted storage
                        Timber.w("Current path is on unmounted storage, navigating to default storage")
                        emitSnackbarEffect(
                            loadStringResourceUseCase.invoke(R.string.storage_removed_navigating_back)
                        )
                        onBackStackEntryChanged(
                            entry = BackStackEntry(path = defaultStorageRootPath)
                        )
                    } else {
                        // Just show a notification
                        path?.let { p ->
                            val volumeName = kotlin.runCatching {
                                getFileNameUseCase(p)
                            }.getOrElse { "External storage" }
                            emitSnackbarEffect(
                                loadStringResourceUseCase.invoke(R.string.storage_removed, volumeName)
                            )
                        }
                    }

                    // Refresh storage overview
                    if (currentPathIsStorageVolumePathUseCase(currentRootPath)) {
                        updateStorageOverview()
                    }
                    // Notify UI that storage volumes list has changed
                    _storageVolumesChanged.emit(Unit)
                }
            }
        }
    }

    suspend fun listRootFiles(): List<String> = suspendCoroutine { continuation ->
        try {
            Timber.d("Listing root files using File API (secure alternative to exec)")
            // SECURITY FIX: Use File API instead of Runtime.exec() which is a security risk
            val rootDir = File("/")
            val files = rootDir.listFiles()
            val fileList = mutableListOf<String>()
            if (files != null) {
                files.map { it.name }.also {
                    Timber.d("Found ${it.size} files/directories in root")
                    fileList.addAll(it)
                }
            } else {
                Timber.w("Cannot list root directory")
                Timber.d("Calling ls /")
                val process = Runtime.getRuntime().exec("ls /")
                val reader = process.inputStream.bufferedReader()
                reader.useLines { lines ->
                    lines.forEach {
                        fileList.add(it)
                        Timber.d("Adding line to fileList $it")
                    }
                }
            }

            continuation.resume(fileList)
        } catch (e: Exception) {
            Timber.e(e, "Error listing root files")
            continuation.resumeWithException(e)
        }
    }

    private fun queryFilesByAbstractPath(
        path: String,
        filterQueries: List<FilterQuery> = emptyList(),
        sortMode: SortMode = SortMode.NAME_AZ
    ) {
        viewModelScope.launch {
            val queryFunc: (suspend () -> List<MediaInfo>)? = when (path) {
                MediaGroup.Audio.path -> queryAllAudiosUseCase::invoke
                MediaGroup.Video.path -> queryAllVideosUseCase::invoke
                MediaGroup.Document.path -> queryAllDocumentsUseCase::invoke
                MediaGroup.Image.path -> queryAllImagesUseCase::invoke
                MediaGroup.Archive.path -> queryAllArchivesUseCase::invoke
                MediaGroup.RecentFiles.path -> queryRecentFilesUseCase::invoke
                MediaGroup.AllFiles.path -> queryAllMediaUseCase::invoke
                MediaGroup.Apk.path -> queryAllApksUseCase::invoke
                MediaGroup.RecycleBin.path -> queryAllFromRecycleBinUseCase::invoke
                MediaGroup.App.path -> queryInstalledApps::invoke
                MediaGroup.Root.path -> {
                    Timber.d("Listing root files")
                    kotlin.runCatching {
                        val files = listRootFiles()
                        Timber.d("Root files: $files")
                    }
                    suspend { emptyList() }
                }

                // Default case: treat as a file system path (supports all storage volumes including external USB drives)
                else -> suspend { queryMediaListFromPathUseCase.invoke(path) }
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
        mediaGroupPath: String,
    ) {
        Timber.d("onMediaGroupClick: $mediaGroupPath")
        Timber.d("The current path is $currentRootPath")
        if (currentRootPath == mediaGroupPath) return
        onBackStackEntryChanged(
            entry = BackStackEntry(
                path = mediaGroupPath,
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
                        // Launch image viewer with only the current image
                        // The viewer will lazy-load adjacent images as needed
                        launchImageViewerIntent(
                            context = context,
                            file = derivedFile,
                            mimeType = mimeData.mimeType,
                            folderPath = currentRootPath
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

    private fun launchImageViewerIntent(
        context: Context,
        file: File,
        mimeType: String?,
        folderPath: String
    ) {
        viewModelScope.launch {
            val type = mimeType.otherwise(kotlin.runCatching { MimeData.fromFile(file) }.getOrNull()?.mimeType)
            val intent = Intent()
            intent.setClass(context, ImageViewerActivity::class.java)
            val uri = file.asContentSchemeUri(context)
            intent.setDataAndType(uri, type)

            // Pass folder path and file path for lazy loading of adjacent images
            intent.putExtra("folder_path", folderPath)
            intent.putExtra("file_path", file.absolutePath)

            context.startActivity(intent)
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
            if (wipeCache) {
                cachedRootMediaInfo = null
                // Invalidate cache for current path
                cacheManager.invalidate(currentRootPath)
            }
            updateStates()
            loadContentByLastBackStackEntry(force)
        }
    }

    fun onFilter(queries: List<FilterQuery>) {
        viewModelScope.launch {
            val entry = backStackEntry.value!!.copy(filterQueries = queries)
            backStacksHolder.updateLastEntryAt(entry = entry)
            onReload(wipeCache = false, force = false)
        }
    }

    fun onLayout(mode: LayoutMode) {
        viewModelScope.launch {
            onAppSettingsChanged(AppSettings::layoutMode, mode)
        }
    }

    fun onSort(sortMode: SortMode) {
        viewModelScope.launch {
            val entry = backStackEntry.value!!.copy(sortMode = sortMode)
            backStacksHolder.updateLastEntryAt(entry = entry)
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
            val entry = backStacksHolder.getEntryAt().otherwise(BackStackEntry.default)
            val path = entry.path ?: defaultStorageRootPath
            val filterHash = entry.filterQueries.hashCode()
            val sortModeKey = entry.sortMode.toString()

            _mediaQueryRequestResponse.emit(Resource.Loading())

            try {
                val result = if (force) {
                    // Force reload: query fresh and update cache
                    val freshData = queryAndProcessData(path, entry.filterQueries, entry.sortMode)
                    cacheManager.putWithFilters(path, filterHash, sortModeKey, freshData)
                    freshData
                } else {
                    // Use cache with automatic fallback to query
                    cacheManager.getOrPutWithFilters(path, filterHash, sortModeKey) {
                        queryAndProcessData(path, entry.filterQueries, entry.sortMode)
                    }
                }

                _mediaQueryRequestResponse.emit(Resource.Success(result))

                // Update root cache if applicable
                if (cachedRootMediaInfo.isNullOrEmpty() && path == defaultStorageRootPath) {
                    cachedRootMediaInfo = result
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading content from path: $path")
                _mediaQueryRequestResponse.emit(
                    Resource.Error(
                        message = "Failed to load content: ${e.message}",
                        cause = e
                    )
                )
            }
        }
    }

    private suspend fun queryAndProcessData(
        path: String,
        filterQueries: List<FilterQuery>,
        sortMode: SortMode
    ): List<MediaInfo> {
        // Determine query function based on path
        val queryFunc: (suspend () -> List<MediaInfo>) = when (path) {
            MediaGroup.Audio.path -> queryAllAudiosUseCase::invoke
            MediaGroup.Video.path -> queryAllVideosUseCase::invoke
            MediaGroup.Document.path -> queryAllDocumentsUseCase::invoke
            MediaGroup.Image.path -> queryAllImagesUseCase::invoke
            MediaGroup.Archive.path -> queryAllArchivesUseCase::invoke
            MediaGroup.RecentFiles.path -> queryRecentFilesUseCase::invoke
            MediaGroup.AllFiles.path -> queryAllMediaUseCase::invoke
            MediaGroup.Apk.path -> queryAllApksUseCase::invoke
            MediaGroup.RecycleBin.path -> queryAllFromRecycleBinUseCase::invoke
            MediaGroup.App.path -> queryInstalledApps::invoke
            MediaGroup.Root.path -> {
                Timber.d("Listing root files")
                kotlin.runCatching {
                    val files = listRootFiles()
                    Timber.d("Root files: $files")
                }
                suspend { emptyList() }
            }
            // Default: treat as file system path
            else -> suspend { queryMediaListFromPathUseCase.invoke(path) }
        }

        // Execute query
        val rawData = queryFunc.invoke()

        // Apply filters and sorting
        val filtered = rawData.filter { mediaInfo ->
            filterQueries.all { filterQuery ->
                filterQuery.filter(mediaInfo, false)
            }
        }
        val sorted = filtered.sortedWith(sortMode)

        return sorted
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
                // Invalidate cache for current path since we added folders
                cacheManager.invalidate(currentRootPath)
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
                // Invalidate cache for current path since we added files
                cacheManager.invalidate(currentRootPath)
                onReload(true)
                _effect.emit(Effect.HideAddMediaDialog)
                emitSnackbarEffect(
                    loadStringResourceUseCase.invoke(R.plurals.file_created_successfully, count),
                )

                _effect.emit(Effect.HideFullscreenLoader)
            }
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

    /**
     * Starts monitoring storage volume mount/unmount events
     */
    fun startStorageMonitoring() {
        storageVolumeMonitorUseCase.startMonitoring()
        // Also start MediaStore monitoring for system-level file changes
        fileChangeMonitorUseCase.startMediaStoreMonitoring()
    }

    /**
     * Stops monitoring storage volume mount/unmount events
     */
    fun stopStorageMonitoring() {
        storageVolumeMonitorUseCase.stopMonitoring()
        fileChangeMonitorUseCase.cleanup()
    }

    /**
     * Handles file change events and invalidates cache accordingly
     */
    private fun handleFileChangeEvent(event: FileChangeEvent) {
        viewModelScope.launch {
            when (event) {
                is FileChangeEvent.Created -> {
                    val parentPath = event.path.substringBeforeLast("/")
                    Timber.i("File created: ${event.path}, invalidating cache for: $parentPath")
                    cacheManager.invalidate(parentPath)
                    if (currentRootPath == parentPath) onReload(wipeCache = false, force = true)
                }

                is FileChangeEvent.Deleted -> {
                    val parentPath = event.path.substringBeforeLast("/")
                    Timber.i("File deleted: ${event.path}, invalidating cache for: $parentPath")
                    cacheManager.invalidate(parentPath)
                    if (currentRootPath == parentPath) onReload(wipeCache = false, force = true)
                }

                is FileChangeEvent.Modified -> {
                    val parentPath = event.path.substringBeforeLast("/")
                    Timber.i("File modified: ${event.path}, invalidating cache for: $parentPath")
                    cacheManager.invalidate(parentPath)
                    if (currentRootPath == parentPath) onReload(wipeCache = false, force = true)
                }

                is FileChangeEvent.MovedFrom -> {
                    val parentPath = event.path.substringBeforeLast("/")
                    Timber.i("File moved from: ${event.path}, invalidating cache for: $parentPath")
                    cacheManager.invalidate(parentPath)
                    if (currentRootPath == parentPath) onReload(wipeCache = false, force = true)
                }

                is FileChangeEvent.MovedTo -> {
                    val parentPath = event.path.substringBeforeLast("/")
                    Timber.i("File moved to: ${event.path}, invalidating cache for: $parentPath")
                    cacheManager.invalidate(parentPath)
                    if (currentRootPath == parentPath) onReload(wipeCache = false, force = true)
                }

                is FileChangeEvent.DirectoryChanged -> {
                    // Batch changes in directory
                    Timber.i("Directory changed: ${event.path} (${event.changeCount} changes)")
                    cacheManager.invalidate(event.path)

                    if (currentRootPath == event.path) {
                        onReload(wipeCache = false, force = true)
                    }
                }

                is FileChangeEvent.MediaStoreChanged -> {
                    // System-level media change - invalidate relevant caches
                    Timber.i("MediaStore changed: ${event.type}")
                    when (event.type) {
                        FileChangeEvent.MediaStoreChanged.MediaType.IMAGES -> {
                            cacheManager.invalidate(MediaGroup.Image.path)
                        }

                        FileChangeEvent.MediaStoreChanged.MediaType.VIDEOS -> {
                            cacheManager.invalidate(MediaGroup.Video.path)
                        }

                        FileChangeEvent.MediaStoreChanged.MediaType.AUDIO -> {
                            cacheManager.invalidate(MediaGroup.Audio.path)
                        }

                        FileChangeEvent.MediaStoreChanged.MediaType.FILES -> {
                            cacheManager.invalidate(MediaGroup.AllFiles.path)
                        }

                        FileChangeEvent.MediaStoreChanged.MediaType.DOWNLOADS -> {
                            cacheManager.invalidate(MediaGroup.RecentFiles.path)
                        }
                    }

                    // Reload if currently viewing affected media group
                    val shouldReload = when (event.type) {
                        FileChangeEvent.MediaStoreChanged.MediaType.IMAGES -> currentRootPath == MediaGroup.Image.path
                        FileChangeEvent.MediaStoreChanged.MediaType.VIDEOS -> currentRootPath == MediaGroup.Video.path
                        FileChangeEvent.MediaStoreChanged.MediaType.AUDIO -> currentRootPath == MediaGroup.Audio.path
                        FileChangeEvent.MediaStoreChanged.MediaType.FILES -> currentRootPath == MediaGroup.AllFiles.path
                        FileChangeEvent.MediaStoreChanged.MediaType.DOWNLOADS -> currentRootPath == MediaGroup.RecentFiles.path
                    }

                    if (shouldReload) {
                        onReload(wipeCache = false, force = true)
                    }
                }
            }
        }
    }

    /**
     * Clear all cache (can be called from settings or manually)
     */
    fun clearAllCache() {
        viewModelScope.launch {
            cacheManager.clearAll()
            cachedRootMediaInfo = null
            Timber.i("All cache cleared")
        }
    }


    override fun onCleared() {
        super.onCleared()
        stopStorageMonitoring()
        clearAllCache()
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