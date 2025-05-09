package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import android.content.Context
import android.os.Environment
import android.os.Parcelable
import android.os.StatFs
import android.os.storage.StorageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import org.koin.core.annotation.Factory
import timber.log.Timber
import java.io.File


@Parcelize
data class StorageVolumeOverview(
    val name: String = "Internal storage",
    val mountPoint: String,
    val path: String = mountPoint,
    val totalSize: Long,
    val usedSize: Long,
    val freeSize: Long,
    val type: String? = null,
    val device: String? = null,
    val isRemovable: Boolean? = null,
    val isPrimary: Boolean? = null,
    val isEmulated: Boolean? = null,
    val lastSeenPath: String? = null,
    val lastSeenPathName: String? = null
) : Parcelable


@Factory
class GetStorageVolumeOverviewUseCase(
    private val context: Context,
    private val currentPathIsStorageVolumePath: CurrentPathIsStorageVolumePathUseCase,
    private val getFileNameUseCase: GetFileNameUseCase,
) {
    suspend operator fun invoke(
        currentPath: String,
        lastSeenPath: String? = null
    ): StorageVolumeOverview = withContext(Dispatchers.IO) {
        deriveStorageOverview(context, currentPath, lastSeenPath)
    }

    private suspend fun deriveStorageOverview(context: Context, path: String, lastSeenPath: String? = null): StorageVolumeOverview {
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

        return StorageVolumeOverview(
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