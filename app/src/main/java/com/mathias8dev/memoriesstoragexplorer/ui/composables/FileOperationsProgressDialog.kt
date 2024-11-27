package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.ui.services.fileOperations.FileOperationProgress
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize
import java.io.File

@Composable
fun FileOperationsProgressDialog(
    progressMap: Map<String, FileOperationProgress>,
    onPause: (uid: String) -> Unit,
    onResume: (uid: String) -> Unit,
    onAbort: (uid: String) -> Unit,
    onAbortAll: () -> Unit,
    onDismiss: () -> Unit,
) {


    StandardDialog(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismiss,
        makeContentScrollable = false
    ) {

        Text(
            text = "File Operations",
            fontSize = 18.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(progressMap.toList()) { _, (uid, progressInfo) ->

                val description by remember(uid) {
                    derivedStateOf {
                        (if (progressInfo.operation.isCopy()) "Copying" else "Moving") + (progressInfo.pathView?.let { " ${it.foldersCount} folders ${it.filesCount} files" } ?: "")
                    }
                }

                Text(
                    text = description,
                    fontSize = 16.sp,
                    lineHeight = 16.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis
                )


                ProgressItem(
                    modifier = Modifier.padding(top = 10.dp),
                    fileOperationProgress = progressInfo,
                    onPause = { onPause(uid) },
                    onAbort = { onAbort(uid) },
                    onResume = { onResume(uid) }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End)
                ) {
                    Button(
                        shape = RectangleShape,
                        onClick = onAbortAll,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "ABORT",
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }

                    Button(
                        shape = RectangleShape,
                        onClick = onDismiss,
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "BACKGROUND",
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }


    }
}


@Composable
private fun ProgressItem(
    modifier: Modifier = Modifier,
    fileOperationProgress: FileOperationProgress,
    onPause: () -> Unit,
    onAbort: () -> Unit,
    onResume: () -> Unit
) {

    val updatedInfo by rememberUpdatedState(fileOperationProgress)

    var expand by rememberSaveable {
        mutableStateOf(false)
    }

    val rotate by animateFloatAsState(
        targetValue = if (expand) 180f else 0f,
        label = "RotateAnimationLabel"
    )


    val srcFile by remember(updatedInfo) {
        derivedStateOf { File(fileOperationProgress.sourceFilePath) }
    }

    val destFile by remember(updatedInfo) {
        derivedStateOf { File(fileOperationProgress.destinationFilePath) }
    }

    val isPaused by remember(updatedInfo) {
        derivedStateOf {
            fileOperationProgress.controller?.isPaused() == true
        }
    }

    val isRunning by remember(updatedInfo) {
        derivedStateOf {
            fileOperationProgress.controller?.isPaused() != true
        }
    }



    Column(
        modifier = modifier.fillMaxWidth()
    ) {

        Text(
            text = destFile.name,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = {
                    if (isRunning) onPause()
                    else onResume()
                }
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = "PausePlay",
                    tint = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Column(
                modifier = Modifier
                    .width(0.dp)
                    .weight(1F)
            ) {
                LinearProgressIndicator(
                    progress = { updatedInfo.progress },
                )

                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "${updatedInfo.totalBytesCopied.asFileReadableSize()}/${(updatedInfo.totalBytesCopied / updatedInfo.progress).asFileReadableSize()}",
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "${updatedInfo.copyRate.asFileReadableSize()}/s",
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }


        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .clickable { expand = !expand }
                .wrapContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                modifier = Modifier.rotate(rotate),
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Chevron"
            )

            Text(
                text = "More",
                fontSize = 16.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        AnimatedVisibility(expand) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                if (fileOperationProgress.isDirectoryCopy) {
                    DescriptionItem(
                        title = "From",
                        description = srcFile.parent ?: srcFile.absolutePath
                    )
                } else {
                    DescriptionItem(
                        title = "From",
                        description = srcFile.absolutePath
                    )
                }

                DescriptionItem(
                    title = "To",
                    description = destFile.parent ?: destFile.absolutePath
                )

                TextButton(
                    shape = RectangleShape,
                    onClick = onAbort
                ) {
                    Text("ABORT")
                }
            }
        }
    }
}

@Composable
private fun DescriptionItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description + "\n",
            fontSize = 14.sp,
            lineHeight = 14.sp,
            color = Color.Gray,
            maxLines = 2
        )
    }
}