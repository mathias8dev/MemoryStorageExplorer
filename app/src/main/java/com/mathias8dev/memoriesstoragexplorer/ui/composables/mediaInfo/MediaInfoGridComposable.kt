package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaInfo

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.ui.composables.HighlightText
import com.mathias8dev.memoriesstoragexplorer.ui.utils.conditional
import com.mathias8dev.memoriesstoragexplorer.ui.utils.mimeData
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toIconResource

@Composable
internal fun MediaInfoGridComposable(
    modifier: Modifier = Modifier,
    mediaInfo: MediaInfo,
    selected: Boolean = false,
    iconSizeDp: Dp = 48.dp,
    innerPaddingValues: PaddingValues = PaddingValues(0.dp),
    layoutMode: LayoutMode = LayoutMode.GRID,
    query: String? = null,
    onClick: ((MediaInfo) -> Unit)? = null,
    onLongClick: ((MediaInfo) -> Unit)? = null,
) {

    val localContext = LocalContext.current

    val file by remember {
        derivedStateOf {
            mediaInfo.privateContentUri!!.toFile()
        }
    }

    val mimeData = remember(file) {
        file.mimeData
    }

    val iconResource = remember(file, mimeData) {
        file.toIconResource(localContext)
    }


    Column(
        modifier = modifier
            .combinedClickable(onClick = {
                onClick?.invoke(mediaInfo)
            }, onLongClick = {
                onLongClick?.invoke(mediaInfo)
            })
            .fillMaxWidth()
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2F)
                else if (layoutMode == LayoutMode.GALLERY) MaterialTheme.colorScheme.secondary.copy(alpha = 0.05F)
                else Color.Transparent
            )
            .padding(paddingValues = if (layoutMode != LayoutMode.GALLERY) innerPaddingValues else PaddingValues(top = 5.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FileIcon(
            mediaInfo = mediaInfo,
            iconSizeDp = iconSizeDp
        )

        val surfaceVariantColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2F)

        HighlightText(
            modifier = Modifier
                .padding(top = 6.dp)
                .conditional(layoutMode == LayoutMode.GALLERY) {
                    this
                        .fillMaxWidth()
                        .background(surfaceVariantColor)
                        .padding(vertical = 8.dp, horizontal = 8.dp)
                },
            text = file.name,
            highlightWith = query,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center
        )

    }
}

