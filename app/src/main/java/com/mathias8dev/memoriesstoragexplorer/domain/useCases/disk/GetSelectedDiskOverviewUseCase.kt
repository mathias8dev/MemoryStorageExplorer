package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import com.mathias8dev.memoriesstoragexplorer.ui.composables.SelectedDiskOverview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import timber.log.Timber
import java.io.File


@Factory
class GetSelectedDiskOverviewUseCase(
    private val context: Context,
    private val currentPathIsStorageVolumePath: CurrentPathIsStorageVolumePathUseCase,
    private val getFileNameUseCase: GetFileNameUseCase
) {
    suspend operator fun invoke(
        currentPath: String,
        lastSeenPath: String? = null
    ): SelectedDiskOverview = withContext(Dispatchers.IO) {
        deriveSelectedDiskOverview(context, currentPath, lastSeenPath)
    }

    private suspend fun deriveSelectedDiskOverview(context: Context, path: String, lastSeenPath: String? = null): SelectedDiskOverview {
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
        // Determine the root path (mount point) for the volume
        val mountPoint = storageVolume?.absolutePathOrNull() ?: file.absolutePath
        val lastSeenPathName = if (!currentPathIsStorageVolumePath(path) && lastSeenPath != null) getFileNameUseCase(lastSeenPath) else null

        Timber.d("The mount point is $mountPoint")

        return SelectedDiskOverview(
            name = name,
            mountPoint = mountPoint,
            totalSize = totalSize,
            usedSize = usedSize,
            freeSize = freeSize,
            type = type,
            device = devicePath,
            lastSeenPath = lastSeenPath,
            lastSeenPathName = lastSeenPathName
        )
    }
}