package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaInfo

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import timber.log.Timber

@Composable
fun MediaInfoComposable(
    modifier: Modifier = Modifier,
    mediaInfo: MediaInfo,
    layoutMode: LayoutMode = LayoutMode.DETAILED,
    selected: Boolean = false,
    iconSizeDp: Dp = 48.dp,
    innerPaddingValues: PaddingValues = PaddingValues(vertical = 10.dp, horizontal = 16.dp),
    query: String? = null,
    onClick: ((MediaInfo) -> Unit)? = null,
    onLongClick: ((MediaInfo) -> Unit)? = null,
) {

    Timber.d("The layout Mode is $layoutMode")
    when (layoutMode) {
        LayoutMode.GRID, LayoutMode.GALLERY -> {
            MediaInfoGridComposable(
                modifier = modifier,
                mediaInfo = mediaInfo,
                selected = selected,
                iconSizeDp = iconSizeDp,
                innerPaddingValues = innerPaddingValues,
                layoutMode = layoutMode,
                query = query,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }

        else -> {
            MediaInfoColumnComposable(
                modifier = modifier,
                mediaInfo = mediaInfo,
                selected = selected,
                iconSizeDp = iconSizeDp,
                layoutMode = layoutMode,
                innerPaddingValues = innerPaddingValues,
                query = query,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }

}
