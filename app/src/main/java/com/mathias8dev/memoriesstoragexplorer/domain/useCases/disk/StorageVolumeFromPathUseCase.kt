package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import android.content.Context
import android.os.Build
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory


@Factory
class StorageVolumeFromPathUseCase(
    private val context: Context
) {

    suspend operator fun invoke(path: String, strictMatch: Boolean = false): StorageVolume? = withContext(Dispatchers.IO) {
        // Get the StorageManager instance
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        // Get the list of available storage volumes
        val storageVolumes: List<StorageVolume> = storageManager.storageVolumes

        // Check if the given path matches any storage volume's root directory
        storageVolumes.find { volume ->
            val volumePath = volume.absolutePathOrNull()
            volumePath != null && if (strictMatch) path == volumePath else volumePath.startsWith(path, true)
        }
    }
}


fun StorageVolume.absolutePathOrNull(): String? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        this.directory?.absolutePath
    } else {
        kotlin.runCatching {
            val volumeClass = StorageVolume::class.java
            val getPath = volumeClass.getMethod("getPath")
            getPath.invoke(this) as? String
        }.getOrNull()
    }
}