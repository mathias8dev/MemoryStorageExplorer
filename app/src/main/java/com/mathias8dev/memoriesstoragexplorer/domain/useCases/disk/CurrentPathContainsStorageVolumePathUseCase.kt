package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import android.content.Context
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory


@Factory
class CurrentPathContainsStorageVolumePathUseCase(
    private val context: Context
) {

    suspend operator fun invoke(path: String): Boolean = withContext(Dispatchers.IO) {
        // Get the StorageManager instance
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        // Get the list of available storage volumes
        val storageVolumes: List<StorageVolume> = storageManager.storageVolumes

        // Check if the given path matches any storage volume's root directory
        storageVolumes.any { volume ->
            val volumePath = volume.absolutePathOrNull()
            volumePath != null && (path == volumePath || path.startsWith(volumePath))
        }
    }
}
