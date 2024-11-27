package com.mathias8dev.memoriesstoragexplorer.ui.screens.mediaList

import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mathias8dev.memoriesstoragexplorer.LocalClipboardHandler
import com.mathias8dev.memoriesstoragexplorer.LocalSnackbarHostState
import com.mathias8dev.memoriesstoragexplorer.domain.FilterQuery
import com.mathias8dev.memoriesstoragexplorer.domain.clipboard.ClipboardHandler
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.domain.services.fileCopy.FileExistsAction
import com.mathias8dev.memoriesstoragexplorer.domain.utils.onLoading
import com.mathias8dev.memoriesstoragexplorer.domain.utils.onSuccess
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ActionMenuCurrentPath
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ActionsMenusFileSelectedComposable
import com.mathias8dev.memoriesstoragexplorer.ui.composables.AddFileDialog
import com.mathias8dev.memoriesstoragexplorer.ui.composables.AddFolderDialog
import com.mathias8dev.memoriesstoragexplorer.ui.composables.AutoGrowTabs
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ClipboardDisplay
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ContextMenuComposable
import com.mathias8dev.memoriesstoragexplorer.ui.composables.FileExistsDialog
import com.mathias8dev.memoriesstoragexplorer.ui.composables.FileOperationsProgressDialog
import com.mathias8dev.memoriesstoragexplorer.ui.composables.FullscreenLoadingDialog
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaGroup
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaGroupHomeComposable
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaListScreenLayout
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaPropertiesDialog
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaRenameDialog
import com.mathias8dev.memoriesstoragexplorer.ui.composables.dialogBackgroundColor
import com.mathias8dev.memoriesstoragexplorer.ui.destinations.SettingsScreenDestination
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.BackStackEntry
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntry
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.ClipboardEntryPayload
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.SharedViewModel
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.mediaList.MediaListComposable
import com.mathias8dev.memoriesstoragexplorer.ui.screens.home.mediaList.MediaListLoadingComposable
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationsAndroidService
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import java.io.File
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Destination
@RootNavGraph
fun MediaListScreen(
    path: String = Environment.getExternalStorageDirectory().absolutePath,
    navigator: DestinationsNavigator
) {

    val viewModel: SharedViewModel = koinViewModel()
    val mediaResponse by viewModel.mediaQueryRequestResponse.collectAsStateWithLifecycle()

    val clipboardHandler = LocalClipboardHandler.current

    val selectedMedia = clipboardHandler.selectedMedia
    val clipboard = clipboardHandler.clipboard

    var loaded by rememberSaveable {
        mutableStateOf(false)
    }

    val localContext = LocalContext.current

    val density = LocalDensity.current


    Timber.d("The local density is: $density")


    val pullRefreshState = rememberPullToRefreshState()

    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            delay(200)
            viewModel.onReload()
            isRefreshing = false
        }
    }

    val pagerState = viewModel.pagerState
    val isCurrentStackEmpty by viewModel.isCurrentStackEmpty
    val currentStackSize by viewModel.currentStackSize
    val tabIndex = viewModel.tabIndex.collectAsStateWithLifecycle()
    val backStackEntry by viewModel.backStackEntry.collectAsStateWithLifecycle()
    val selectedDiskOverview = viewModel.selectedDiskOverview
    val currentRootPathView = viewModel.currentPathBaseStat
    val currentRootPath = viewModel.currentRootPath

    val progressMap = clipboardHandler.fileOperationsProgress
    val skippedList = clipboardHandler.skippedOperations

    Timber.d("MediaListScreenProgressMap: $progressMap")

    var showProgressionDialog by remember(progressMap?.keys?.size) {
        Timber.d("showProgressionDialog: ${progressMap?.isNotEmpty() == true}")
        mutableStateOf(progressMap?.isNotEmpty() == true)
    }

    val showSkippedQuestionDialog by remember(skippedList?.firstOrNull()) {
        derivedStateOf {
            skippedList?.isNotEmpty() == true
        }
    }

    val searchTerm by remember(backStackEntry?.filterQueries) {
        derivedStateOf {
            (backStackEntry?.filterQueries?.find { it is FilterQuery.SearchTerm } as? FilterQuery.SearchTerm)?.term
        }
    }


    var showRenameMediaDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showFullScreenLoadingDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showAddFileDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showAddFolderDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var showPropertiesDialog by rememberSaveable {
        mutableStateOf(false)
    }

    var mediaToRename: MediaInfo? by rememberSaveable {
        mutableStateOf(null)
    }

    val currentMediaGroup by remember(currentRootPath) {
        derivedStateOf {
            MediaGroup.fromPath(currentRootPath).takeIf { it != MediaGroup.InternalStorage }
        }
    }

    val localSnackbarHostState = LocalSnackbarHostState.current
    val localUriHandler = LocalUriHandler.current


    LaunchedEffect(Unit) {
        val scope = this
        clipboardHandler.listen {
            Timber.d("Event received: $it")
            when (it) {
                is ClipboardHandler.ClipboardEvent.EndDelete,
                is ClipboardHandler.ClipboardEvent.EndSelectedMediaRenamed,
                is ClipboardHandler.ClipboardEvent.EndClipboardEntryClick,
                is ClipboardHandler.ClipboardEvent.EndResolveSkippedOperations,
                is ClipboardHandler.ClipboardEvent.EndClipboardEntryPayloadClick -> {
                    scope.launch {
                        showRenameMediaDialog = false
                        delay(1.seconds)
                        viewModel.onReload()
                    }
                }

                is ClipboardHandler.ClipboardEvent.CutActionImpossible,
                is ClipboardHandler.ClipboardEvent.ActionImpossible -> {
                    scope.launch {
                        localSnackbarHostState.showSnackbar("Operation not permitted")
                    }
                }

                is ClipboardHandler.ClipboardEvent.StartRename -> {
                    showRenameMediaDialog = true
                    mediaToRename = it.mediaInfo
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        val scope = this
        Timber.d("Listening to file operations")
        clipboardHandler.onFileOperationEvent {
            Timber.d("FileOperationEvent: $it")
            when (it) {
                is FileOperationsAndroidService.FileOperationsEvent.ResultEvent -> {
                    if (it.status == FileOperationsAndroidService.Status.NOT_PERMITTED) {
                        Timber.d("Operation not permitted 3")
                        localSnackbarHostState.showSnackbar("Operation not permitted")
                    }
                }

                else -> Unit
            }
        }
    }


    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest {
            when (it) {
                is SharedViewModel.Effect.ShowToast -> {
                    Toast.makeText(
                        localContext,
                        it.message,
                        it.duration
                    ).show()

                }

                is SharedViewModel.Effect.ShowSnackBar -> {
                    localSnackbarHostState.showSnackbar(
                        message = it.message,
                        duration = it.duration,
                    )
                }


                SharedViewModel.Effect.HideFullscreenLoader -> {
                    showFullScreenLoadingDialog = false
                }


                SharedViewModel.Effect.ShowFullscreenLoader -> {
                    showFullScreenLoadingDialog = true
                }

                SharedViewModel.Effect.HideAddMediaDialog -> {
                    showAddFileDialog = false
                    showAddFolderDialog = false
                }

                SharedViewModel.Effect.ShowAddFileDialog -> {
                    showAddFileDialog = true
                }

                SharedViewModel.Effect.ShowAddFolderDialog -> {
                    showAddFolderDialog = true
                }

            }
        }
    }

    var firstBack = rememberSaveable {
        -1L
    }

    BackHandler(currentStackSize == 1) {
        coroutineScope.launch {
            val now = System.currentTimeMillis()
            if (now - firstBack <= 3000) {
                navigator.popBackStack()
            } else {
                firstBack = now
                localSnackbarHostState.showSnackbar("Press back again to exit")
            }
        }
    }


    BackHandler(currentStackSize > 1 || selectedMedia.isNotEmpty()) {
        Timber.d("BackHandler - isCurrentStackEmpty: $isCurrentStackEmpty, clipboard: $clipboard")
        if (selectedMedia.isEmpty()) viewModel.onPopCurrentBackStack()
        else clipboardHandler.onRemoveAllSelectedMedia()
    }

    LaunchedEffect(Unit) {
        if (!loaded) {
            viewModel.onBackStackEntryChanged(
                entry = BackStackEntry(
                    path = path,
                )
            )
            loaded = true
        }
    }



    MediaListScreenLayout(
        currentQuery = searchTerm,
        onNavigateToMediaGroup = {
            Timber.d("OnNavigate to media group: $it")
            viewModel.onMediaGroupClick(it)
        },
        onAdd = viewModel::onAdd,
        onFilter = viewModel::onFilter,
        onSort = viewModel::onSort,
        onReload = viewModel::onReload,
        onUpdateLayout = viewModel::onLayout,
        onSelectAll = {
            mediaResponse.onSuccess { mediaList ->
                clipboardHandler.onAddAllMedia(mediaList)
            }
        },
        title = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column {
                    if (selectedMedia.isEmpty()) {
                        if (currentRootPathView != null) {
                            ActionMenuCurrentPath(
                                view = currentRootPathView,
                                selectedDiskOverview = selectedDiskOverview
                            )
                        }
                    }


                    // SelectedMedia is not empty, in selection mode
                    AnimatedVisibility(selectedMedia.isNotEmpty()) {
                        val selectedMediaSize by remember(selectedMedia) {
                            derivedStateOf {
                                selectedMedia.sumOf { it.size }
                            }
                        }

                        ActionsMenusFileSelectedComposable(
                            modifier = Modifier
                                .fillMaxWidth(0.89F),
                            selectedFilesSize = selectedMediaSize,
                            selectedFilesCount = selectedMedia.size,
                            onUnselectAllClick = {
                                clipboardHandler.onRemoveAllSelectedMedia()
                            },
                            onDeleteClick = {
                                clipboardHandler.onDelete()
                            },
                            onCopyClick = {
                                clipboardHandler.onCopyToClipboard()
                            },
                            onCutClick = {
                                clipboardHandler.onCutToClipboard()
                            },
                            onRenameClick = {
                                clipboardHandler.onRename()
                            },
                        )
                    }
                }


                Spacer(modifier = Modifier.weight(1F))


                AnimatedVisibility(selectedMedia.isEmpty() && clipboard.isNotEmpty()) {
                    // An action has been done after the selection, the clipboard is not empty. Only the paste action is available

                    var showContextMenu by rememberSaveable {
                        mutableStateOf(false)
                    }
                    ContextMenuComposable(
                        menuModifier = Modifier
                            .padding(horizontal = 16.dp)
                            .shadow(2.dp, shape = RoundedCornerShape(8.dp))
                            .clip(RoundedCornerShape(8.dp))
                            .background(dialogBackgroundColor()),
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false },
                        onExpandedChange = { showContextMenu = it },
                        actionHolder = {
                            IconButton(
                                onClick = it
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null
                                )
                            }
                        },
                        onDrawMenu = {
                            ClipboardDisplay(
                                modifier = Modifier.background(
                                    color = dialogBackgroundColor(),
                                    shape = RoundedCornerShape(4.dp)
                                ),
                                clipboard = clipboard,
                                onClearEntry = {
                                    clipboardHandler.onClearClipboardByUid(it.uid)
                                    showContextMenu = false
                                },
                                onClearAll = {
                                    clipboardHandler.onClearAllClipboardEntry()
                                    showContextMenu = false
                                },
                                onEntryClick = {
                                    currentRootPathView?.path.let { path ->
                                        if (it.status == ClipboardEntry.Status.STARTED) showProgressionDialog = true
                                        else {
                                            clipboardHandler.onClipboardEntryClick(it, path)
                                            showContextMenu = false
                                        }
                                    }
                                },
                                onEntryPayloadClick = { entry, payload ->
                                    currentRootPathView?.path.let { path ->
                                        if (entry.status == ClipboardEntry.Status.STARTED || payload.status == ClipboardEntryPayload.Status.STARTED) showProgressionDialog = true
                                        else {
                                            clipboardHandler.onClipboardEntryPayloadClick(entry, payload, path)
                                            showContextMenu = false
                                        }
                                    }
                                }
                            )
                        }

                    )

                }

                var showContextMenu by rememberSaveable {
                    mutableStateOf(false)
                }

                ContextMenuComposable(
                    menuModifier = Modifier
                        .padding(horizontal = 16.dp)
                        .wrapContentWidth()
                        .shadow(8.dp, shape = RoundedCornerShape(8.dp))
                        .background(dialogBackgroundColor()),
                    expanded = showContextMenu,
                    onDismissRequest = { showContextMenu = false },
                    onExpandedChange = { showContextMenu = it },
                    actionHolder = {
                        IconButton(
                            onClick = it
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null
                            )
                        }
                    },
                    onDrawMenu = {

                        if (selectedMedia.isNotEmpty()) {
                            Text(
                                modifier = Modifier
                                    .clickable {
                                        showContextMenu = false
                                        showPropertiesDialog = true
                                    }
                                    .wrapContentWidth()
                                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 72.dp),
                                text = "Properties",
                                fontSize = 14.sp,
                                lineHeight = 14.sp
                            )
                        }

                        if (selectedMedia.size == 1 && selectedMedia.firstOrNull()?.privateContentUri?.toFile()?.isFile == true) {
                            Text(
                                modifier = Modifier
                                    .clickable {
                                        showContextMenu = false
                                        selectedMedia.firstOrNull()?.contentUri
                                            ?.toString()
                                            ?.let { localUriHandler.openUri(it) }
                                    }
                                    .wrapContentWidth()
                                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 72.dp),
                                text = "Open with",
                                fontSize = 14.sp,
                                lineHeight = 14.sp
                            )
                        }

                        Text(
                            modifier = Modifier
                                .clickable {
                                    showContextMenu = false
                                    navigator.navigate(SettingsScreenDestination)
                                }
                                .wrapContentWidth()
                                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 72.dp),
                            text = "Settings",
                            fontSize = 14.sp,
                            lineHeight = 14.sp
                        )

                    }
                )

            }
        }
    ) { innerPadding, _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullToRefresh(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                )
        ) {

            AutoGrowTabs(
                selectedIndex = tabIndex.value,
                tabs = viewModel.tabs,
                tabToString = { it.title },
                tabToKey = { _, tab -> tab.id },
                selectedColor = MaterialTheme.colorScheme.primary,
                onRemoveTabAt = viewModel::onTabRemovedAt,
                onTabClicked = viewModel::onTabIndexChanged
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                HorizontalPager(
                    state = pagerState,
                ) {
                    key(backStackEntry?.uid) {
                        if (currentMediaGroup == MediaGroup.Home) {
                            MediaGroupHomeComposable(
                                modifier = Modifier.padding(top = 16.dp),
                                onMediaGroupClick = viewModel::onMediaGroupClick
                            )
                        } else {
                            mediaResponse.onLoading {
                                MediaListLoadingComposable()
                            }
                            mediaResponse.onSuccess { mediaList ->
                                MediaListComposable(
                                    mediaList = mediaList,
                                    currentQuery = searchTerm,
                                    selectedMedia = selectedMedia,
                                    onMediaLongClick = clipboardHandler::onAddMedia,
                                    onMediaClick = { mediaInfo ->
                                        if (selectedMedia.isNotEmpty()) {
                                            if (selectedMedia.contains(mediaInfo)) {
                                                clipboardHandler.onRemoveSelectedMedia(mediaInfo)
                                            } else {
                                                clipboardHandler.onAddMedia(mediaInfo)
                                            }
                                        } else {
                                            viewModel.onMediaClick(
                                                mediaInfo = mediaInfo,
                                                context = localContext,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                PullToRefreshDefaults.Indicator(
                    state = pullRefreshState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-32).dp),
                    isRefreshing = isRefreshing
                )

            }
        }
    }

    if (showPropertiesDialog) {
        MediaPropertiesDialog(
            media = selectedMedia,
            onDismissRequest = {
                showPropertiesDialog = false
            }
        )
    }


    if (showAddFolderDialog) {
        AddFolderDialog(
            onSave = viewModel::onAddFolder,
            onDismissRequest = {
                showAddFolderDialog = false
            }
        )
    }

    if (showAddFileDialog) {
        AddFileDialog(
            onSave = viewModel::onAddFile,
            onDismissRequest = {
                showAddFileDialog = false
            }
        )
    }

    if (showRenameMediaDialog) {
        mediaToRename?.let { mediaInfo ->
            MediaRenameDialog(
                mediaInfo = mediaInfo,
                onDismissRequest = {
                    showRenameMediaDialog = false
                    mediaToRename = null
                },
                onRenameClick = { newName ->
                    clipboardHandler.onSelectedMediaRenamed(newName)
                    showRenameMediaDialog = false
                }
            )
        }
    }

    if (showProgressionDialog && progressMap?.isNotEmpty() == true) {
        FileOperationsProgressDialog(
            progressMap = progressMap,
            onPause = clipboardHandler::pauseExecutionByUids,
            onResume = clipboardHandler::resumeExecutionByUids,
            onAbort = clipboardHandler::stopExecutionByUids,
            onAbortAll = {
                clipboardHandler.stopExecutionByUids(*progressMap.keys.toTypedArray())
                showProgressionDialog = false
            },
            onDismiss = {
                showProgressionDialog = false
            }
        )
    }

    if (showSkippedQuestionDialog && skippedList?.isNotEmpty() == true) {

        skippedList.firstOrNull()?.let { operation ->

            val file by remember(operation.uid) {
                derivedStateOf {
                    File(operation.destinationFilePath)
                }
            }

            FileExistsDialog(
                file = file,
                onSkip = {
                    clipboardHandler.resolveSkippedOperation(
                        uid = operation.uid,
                        updatedAction = FileExistsAction.SKIP,
                        remembered = it
                    )
                },
                onReplace = {
                    clipboardHandler.resolveSkippedOperation(
                        uid = operation.uid,
                        updatedAction = FileExistsAction.OVERWRITE,
                        remembered = it
                    )
                },
                onRename = {
                    clipboardHandler.resolveSkippedOperation(
                        uid = operation.uid,
                        updatedAction = FileExistsAction.RENAME,
                        remembered = it
                    )
                }
            )
        }

    }


    if (showFullScreenLoadingDialog) {
        FullscreenLoadingDialog()
    }
}

