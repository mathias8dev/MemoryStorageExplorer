package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File


@Factory
class GetFileNameUseCase(
    private val context: Context
) {

    suspend operator fun invoke(path: String): String = withContext(Dispatchers.IO) {
        val file = File(path)
        if (!file.exists()) "Unknown"
        else {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            val storageVolumes: List<StorageVolume> = storageManager.storageVolumes

            // Find the storage volume matching the current root path
            val matchingVolume = storageVolumes.find { volume ->
                volume.absolutePathOrNull() == file.absolutePath
            }

            // Get the name of the storage volume
            val name = when {
                matchingVolume != null -> matchingVolume.getDescription(context) // Use the description from the StorageVolume
                path == Environment.getExternalStorageDirectory().absolutePath -> "Internal Storage" // Fallback to internal storage
                else -> file.name // Fallback to file name if no match
            }

            name
        }

    }
}