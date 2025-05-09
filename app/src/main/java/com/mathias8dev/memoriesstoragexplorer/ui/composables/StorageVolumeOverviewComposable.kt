package com.mathias8dev.memoriesstoragexplorer.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk.StorageVolumeOverview
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize


@Composable
fun StorageVolumeOverviewComposable(
    modifier: Modifier = Modifier,
    selectedDiskOverview: StorageVolumeOverview
) {
    val state by rememberUpdatedState(selectedDiskOverview)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.wrapContentSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = state.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 16.sp,
                )
                Text(
                    text = "Total: ${state.totalSize.asFileReadableSize()}",
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
                Text(
                    text = "Free: ${state.freeSize.asFileReadableSize()}",
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
                Text(
                    text = "Mount point: ${state.mountPoint}",
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
                state.device?.let {
                    Text(
                        text = "Device: ${state.device}",
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                }
                state.type?.let {
                    Text(
                        text = "Type: ${state.type}",
                        fontSize = 12.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        state.lastSeenPathName?.let { name ->
            Row(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                )
                Text(
                    text = name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}


@Preview
@Composable
private fun StorageOverviewComposablePreview() {
    StorageVolumeOverviewComposable(
        selectedDiskOverview = StorageVolumeOverview(
            name = "Internal storage",
            mountPoint = "/storage/emulated/0",
            totalSize = 1024 * 1024 * 1024,
            usedSize = 1024 * 1024 * 1024,
            freeSize = 1024 * 1024 * 1024,
        )
    )

}