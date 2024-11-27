package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize


@Composable
fun ActionsMenusFileSelectedComposable(
    modifier: Modifier = Modifier,
    selectedFilesSize: Long,
    selectedFilesCount: Int,
    onUnselectAllClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCopyClick: () -> Unit,
    onCutClick: () -> Unit,
    onRenameClick: () -> Unit,
) {

    val fileCount by rememberUpdatedState(selectedFilesCount)


    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onUnselectAllClick) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
            )
        }

        Column(
            modifier = Modifier
                .wrapContentSize()
                .weight(1F),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "$fileCount",
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = selectedFilesSize.asFileReadableSize(),
                fontSize = 12.sp,
                lineHeight = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier.wrapContentWidth()
        ) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                )
            }

            IconButton(onClick = onCopyClick) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null
                )
            }

            IconButton(onClick = onCutClick) {
                Icon(
                    imageVector = Icons.Default.ContentCut,
                    contentDescription = null
                )
            }

            IconButton(onClick = onRenameClick) {
                Icon(
                    imageVector = Icons.Default.TextFormat,
                    contentDescription = null
                )
            }

        }
    }
}
