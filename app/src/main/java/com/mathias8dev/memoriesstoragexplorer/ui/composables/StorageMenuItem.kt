package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize
import java.io.File


@Composable
fun StorageMenuItem(
    file: File,
    title: String,
    onClick: () -> Unit
) {
    val totalBytes by remember(file) {
        derivedStateOf { file.totalSpace }
    }
    val totalFreeBytes by remember(file) {
        derivedStateOf { file.freeSpace }
    }
    val totalUsedBytes by remember(totalBytes, totalFreeBytes) {
        derivedStateOf {
            totalBytes - totalFreeBytes
        }
    }

    MediaGroupComposable2(
        title = title,
        subTitle = {
            LinearProgressIndicator(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                progress = { totalUsedBytes.toFloat() / totalBytes.toFloat() },
            )
            Text(
                text = "${totalBytes.asFileReadableSize()}, ${totalFreeBytes.asFileReadableSize()} Free",
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = Color.Gray
            )
            Text(
                text = file.absolutePath,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                color = Color.Gray
            )
        },
        onClick = onClick
    )
}