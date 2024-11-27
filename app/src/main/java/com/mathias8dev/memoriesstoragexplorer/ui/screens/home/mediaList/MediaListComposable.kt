package com.mathias8dev.memoriesstoragexplorer.ui.screens.home.mediaList

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.data.event.BroadcastEvent
import com.mathias8dev.memoriesstoragexplorer.data.event.EventBus
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaInfoComposable
import com.mathias8dev.memoriesstoragexplorer.ui.composables.MediaInfoLoadingComposable
import my.nanihadesuka.compose.LazyColumnScrollbar
import timber.log.Timber


@Composable
fun MediaListComposable(
    modifier: Modifier = Modifier,
    currentQuery: String? = null,
    mediaList: List<MediaInfo>,
    selectedMedia: List<MediaInfo>,
    onMediaClick: (MediaInfo) -> Unit,
    onMediaLongClick: ((MediaInfo) -> Unit)? = null,
) {


    val listState = rememberLazyListState()

    LaunchedEffect(mediaList, currentQuery) {
        EventBus.publish(BroadcastEvent.HideShowBottomActionsEvent(true))
    }

    LazyColumnScrollbar(state = listState) {
        LazyColumn(
            modifier = modifier.padding(top = 16.dp),
            state = listState
        ) {
            itemsIndexed(items = mediaList, key = { _, media -> media.privateContentUri!! }) { _, media ->
                Modifier
                    .padding(vertical = 0.25.dp)
                MediaInfoComposable(
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null),
                    innerPaddingValues = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
                    selected = selectedMedia.find { it == media } != null,
                    mediaInfo = media,
                    query = currentQuery,
                    onClick = {
                        onMediaClick(media)
                    },
                    onLongClick = onMediaLongClick
                )
            }

            if (mediaList.isEmpty()) {
                item {
                    Timber.d("Is empty")
                    NoItemComposable(
                        modifier = Modifier.fillParentMaxSize()
                    )
                }
            }
        }
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