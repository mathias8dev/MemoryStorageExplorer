package com.mathias8dev.memoriesstoragexplorer.ui.screens.home.mediaList

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.data.event.BroadcastEvent
import com.mathias8dev.memoriesstoragexplorer.data.event.EventBus
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaInfo.MediaInfoComposable
import com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaInfo.MediaInfoLoadingComposable
import com.mathias8dev.memoriesstoragexplorer.ui.utils.pxToDp
import my.nanihadesuka.compose.LazyVerticalGridScrollbar
import timber.log.Timber


@Composable
fun MediaListComposable(
    modifier: Modifier = Modifier,
    currentQuery: String? = null,
    layoutMode: LayoutMode = LayoutMode.COLUMNED,
    mediaList: List<MediaInfo>,
    selectedMedia: List<MediaInfo>,
    onMediaClick: (MediaInfo) -> Unit,
    onMediaLongClick: ((MediaInfo) -> Unit)? = null,
) {


    val gridState = rememberLazyGridState()

    Timber.d("The layout mode is $layoutMode")

    val screenWidthDp = LocalWindowInfo.current.containerSize.width.pxToDp().value.toInt()

    val itemsCount by remember(layoutMode) {
        derivedStateOf {
            when (layoutMode) {
                LayoutMode.COLUMNED, LayoutMode.COMPACT, LayoutMode.DETAILED, LayoutMode.MINIMAL -> 1
                LayoutMode.WRAPPED -> 2
                else -> {
                    Timber.d("On grid layout; the screenWidthDp = $screenWidthDp")
                    val it = screenWidthDp / 90
                    Timber.d("The count is $it")
                    it
                }
            }
        }
    }


    val cells by remember(itemsCount) {
        derivedStateOf {
            GridCells.Fixed(itemsCount)
        }
    }

    LaunchedEffect(mediaList, currentQuery) {
        EventBus.publish(BroadcastEvent.HideShowBottomActionsEvent(true))
    }

    AnimatedVisibility(mediaList.isNotEmpty()) {
        LazyVerticalGridScrollbar(state = gridState) {
            LazyVerticalGrid(
                modifier = modifier
                    .padding(top = 16.dp),
                state = gridState,
                columns = cells,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                itemsIndexed(items = mediaList, key = { _, media -> media.privateContentUri!! }) { _, media ->
                    MediaInfoComposable(
                        modifier = Modifier
                            .height(IntrinsicSize.Max)
                            .animateItem(),
                        layoutMode = layoutMode,
                        selected = selectedMedia.find { it == media } != null,
                        mediaInfo = media,
                        query = currentQuery,
                        onClick = {
                            onMediaClick(media)
                        },
                        onLongClick = onMediaLongClick
                    )
                }
            }
        }
    }

    AnimatedVisibility(mediaList.isEmpty()) {
        NoItemComposable()
    }
}


@Composable
fun MediaListLoadingComposable(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        repeat(10) {
            MediaInfoLoadingComposable(
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}