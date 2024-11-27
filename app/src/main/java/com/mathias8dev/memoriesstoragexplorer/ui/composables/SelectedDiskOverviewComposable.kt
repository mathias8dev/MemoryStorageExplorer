package com.mathias8dev.memoriesstoragexplorer.ui.composables

import android.content.Context
import android.os.Environment
import android.os.Parcelable
import android.os.StatFs
import android.os.storage.StorageManager
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mathias8dev.memoriesstoragexplorer.ui.utils.asFileReadableSize
import kotlinx.parcelize.Parcelize
import java.io.File


fun deriveSelectedDiskOverview(context: Context, path: String, lastSeenPath: String = path): SelectedDiskOverview {
    val file = File(path)

    // Get the storage stats
    val statFs = StatFs(file.absolutePath)

    // Retrieve sizes
    val blockSize = statFs.blockSizeLong
    val totalBlocks = statFs.blockCountLong
    val availableBlocks = statFs.availableBlocksLong

    // Calculate sizes
    val totalSize = totalBlocks * blockSize
    val freeSize = availableBlocks * blockSize
    val usedSize = totalSize - freeSize

    // Use StorageManager to retrieve storage information
    val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    val storageVolume = storageManager.getStorageVolume(file)

    // Retrieve the storage device (e.g., /dev/fuse)
    val devicePath = storageVolume?.uuid ?: "/dev/fuse" // Assuming FUSE if no UUID

    // Check if it's a FUSE file system
    val type = when (devicePath) {
        "/dev/fuse" -> "fuse"
        else -> if (Environment.isExternalStorageEmulated(file)) "Emulated" else "Physical"
    }

    // Set the name of the storage volume (e.g., "SD Card" or "Internal storage")
    val name = storageVolume?.getDescription(context) ?: "Unknown Storage"

    return SelectedDiskOverview(
        name = name,
        mountPoint = file.absolutePath,
        totalSize = totalSize,
        usedSize = usedSize,
        freeSize = freeSize,
        type = type,
        device = devicePath,
        lastSeenPath = lastSeenPath
    )
}


@Parcelize
data class SelectedDiskOverview(
    val name: String = "Internal storage",
    val mountPoint: String,
    val totalSize: Long,
    val usedSize: Long,
    val freeSize: Long,
    val type: String? = null,
    val device: String? = null,
    val lastSeenPath: String? = null,
    val lastSeenPathName: String? = null
) : Parcelable

@Composable
fun SelectedDiskOverviewComposable(
    modifier: Modifier = Modifier,
    selectedDiskOverview: SelectedDiskOverview
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