package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toFile
import com.mathias8dev.memoriesstoragexplorer.R
import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isImageMimeType
import com.mathias8dev.memoriesstoragexplorer.ui.utils.isMusicDirectory
import com.mathias8dev.memoriesstoragexplorer.ui.utils.mimeData
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toFileFormat
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toIconResource
import com.mathias8dev.memoriesstoragexplorer.ui.utils.toReadableSize
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaInfoComposable(
    modifier: Modifier = Modifier,
    mediaInfo: MediaInfo,
    selected: Boolean = false,
    iconSizeDp: Dp = 48.dp,
    showFullDetails: Boolean = true,
    showSimpleDetails: Boolean = showFullDetails,
    showLittleDetails: Boolean = showFullDetails,
    innerPaddingValues: PaddingValues = PaddingValues(0.dp),
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

    val lastModifiedDate = remember(file) {
        LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.of("Europe/Paris"))
    }

    Row(
        modifier = modifier
            .combinedClickable(
                onClick = {
                    onClick?.invoke(mediaInfo)
                },
                onLongClick = {
                    onLongClick?.invoke(mediaInfo)
                }
            )
            .fillMaxWidth()
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2F)
                else Color.Transparent
            )
            .padding(innerPaddingValues)
    ) {
        Card(modifier = Modifier.size(iconSizeDp)) {
            Box {
                when {
                    mimeData?.isImage == true || mimeData?.isVideo == true || file.isImageMimeType() -> {
                        ImageLoaderComposable(
                            model = file,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(iconSizeDp)
                        )
                    }

                    else -> {
                        ImageLoaderComposable(
                            model = iconResource,
                            modifier = Modifier
                                .padding(if (file.isDirectory) 0.dp else 4.dp)
                                .size(iconSizeDp)
                        )
                    }
                }

                if (file.isMusicDirectory()) {
                    Image(
                        painter = painterResource(R.drawable.ic_music_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 3.dp, y = 2.dp)
                            .size(iconSizeDp - 8.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(0.dp)
                    .weight(1F),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {

                HighlightText(
                    text = file.name,
                    highlightWith = query,
                )

                AnimatedVisibility(showSimpleDetails || showFullDetails) {
                    Text(
                        text = lastModifiedDate.toFileFormat(),
                        fontSize = 12.sp
                    )
                }
            }

            AnimatedVisibility(showFullDetails || showLittleDetails) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(bottom = 18.dp),
                    text = if (file.isDirectory) "(${(file.listFiles()?.size ?: 0)})" else file.toReadableSize(),
                    fontSize = 14.sp
                )
            }
        }
    }
}


@Composable
fun MediaInfoLoadingComposable(
    modifier: Modifier = Modifier,
    innerPaddingValues: PaddingValues = PaddingValues(0.dp),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(innerPaddingValues)
    ) {
        // Placeholder for the file icon or image
        Card(modifier = Modifier.size(48.dp)) {
            ShimmerAnimation(
                modifier = Modifier.size(48.dp)
            )
        }

        Row(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .width(0.dp)
                    .weight(1F),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Placeholder for the file name
                ShimmerAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp) // Simulate text height
                )

                // Placeholder for the last modified date
                ShimmerAnimation(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(15.dp) // Simulate smaller text
                )
            }

            // Placeholder for the file size or item count
            ShimmerAnimation(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(bottom = 18.dp)
                    .size(30.dp, 15.dp) // Simulate smaller text
            )
        }
    }
}