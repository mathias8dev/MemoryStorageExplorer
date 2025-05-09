package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import android.content.Context
import android.os.storage.StorageManager
import org.koin.core.annotation.Factory


@Factory
class GetStorageVolumesUseCase(
    private val context: Context,
    private val getStorageVolumeOverviewUseCase: GetStorageVolumeOverviewUseCase
) {

    suspend operator fun invoke(): List<StorageVolumeOverview> {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        return storageManager
            .storageVolumes.filterNotNull().mapNotNull {
                it.absolutePathOrNull()?.let { path ->
                    getStorageVolumeOverviewUseCase(path).copy(
                        isRemovable = it.isRemovable,
                        isPrimary = it.isPrimary,
                        isEmulated = it.isEmulated,
                    )
                }
            }
    }
}