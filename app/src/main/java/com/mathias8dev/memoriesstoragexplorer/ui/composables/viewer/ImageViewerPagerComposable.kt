package com.mathias8dev.memoriesstoragexplorer.ui.composables.viewer

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Image viewer with horizontal swipe navigation between images
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewerPagerComposable(
    modifier: Modifier = Modifier,
    imageUris: List<Uri>,
    initialPage: Int = 0
) {
    val pagerState = rememberPagerState(
        initialPage = initialPage.coerceIn(0, imageUris.size - 1),
        pageCount = { imageUris.size }
    )

    var isImageZoomed by remember { mutableStateOf(false) }

    // Reset zoom state when page changes
    LaunchedEffect(pagerState.currentPage) {
        isImageZoomed = false
    }

    Box(modifier = modifier) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // Allow pager scroll only when images are not zoomed
            userScrollEnabled = !isImageZoomed,
            pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                state = pagerState,
                orientation = androidx.compose.foundation.gestures.Orientation.Horizontal
            )
        ) { page ->
            ZoomableImageViewerComposable(
                modifier = Modifier.fillMaxSize(),
                model = imageUris[page],
                onZoomChanged = { zoomed ->
                    isImageZoomed = zoomed
                }
            )
        }
    }
}
