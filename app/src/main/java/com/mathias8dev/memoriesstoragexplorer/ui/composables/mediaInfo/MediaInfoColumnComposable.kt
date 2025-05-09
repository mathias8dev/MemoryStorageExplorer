package com.mathias8dev.memoriesstoragexplorer.ui.composables.mediaInfo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.domain.enums.LayoutMode
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.ui.composables.HighlightText
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toFileFormat
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toReadableSize
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


@Composable
internal fun MediaInfoColumnComposable(
    modifier: Modifier = Modifier,
    mediaInfo: MediaInfo,
    selected: Boolean = false,
    iconSizeDp: Dp = 48.dp,
    layoutMode: LayoutMode = LayoutMode.DETAILED,
    innerPaddingValues: PaddingValues = PaddingValues(0.dp),
    query: String? = null,
    onClick: ((MediaInfo) -> Unit)? = null,
    onLongClick: ((MediaInfo) -> Unit)? = null,
) {


    val file by remember {
        derivedStateOf {
            mediaInfo.privateContentUri!!.toFile()
        }
    }


    val lastModifiedDate = remember(file) {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("Europe/Paris"))
    }

    Row(
        modifier = modifier
            .combinedClickable(onClick = {
                onClick?.invoke(mediaInfo)
            }, onLongClick = {
                onLongClick?.invoke(mediaInfo)
            })
            .fillMaxWidth()
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2F)
                else Color.Transparent
            )
            .padding(innerPaddingValues)
    ) {
        FileIcon(
            mediaInfo = mediaInfo,
            iconSizeDp = iconSizeDp
        )

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(fraction = if (layoutMode == LayoutMode.WRAPPED) 1F else if (layoutMode == LayoutMode.DETAILED) 0.8F else 0.45F),
                verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically)
            ) {

                HighlightText(
                    text = file.name,
                    highlightWith = query,
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )

                AnimatedVisibility(layoutMode == LayoutMode.DETAILED || layoutMode == LayoutMode.WRAPPED) {
                    Text(
                        text = lastModifiedDate.toFileFormat(),
                        fontSize = 12.sp,
                        lineHeight = 14.sp
                    )
                }
            }


            AnimatedVisibility(
                visible = layoutMode == LayoutMode.COLUMNED,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(1F)
            ) {
                Text(
                    text = lastModifiedDate.toFileFormat("dd MMM yyyy"),
                    fontSize = 10.sp,
                    lineHeight = 12.sp
                )
            }

            AnimatedVisibility(
                visible = layoutMode == LayoutMode.DETAILED || layoutMode == LayoutMode.COLUMNED || layoutMode == LayoutMode.COMPACT,
                modifier = Modifier
            ) {
                Text(
                    text = if (file.isDirectory) "(${(file.listFiles()?.size ?: 0)})" else file.toReadableSize(),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

