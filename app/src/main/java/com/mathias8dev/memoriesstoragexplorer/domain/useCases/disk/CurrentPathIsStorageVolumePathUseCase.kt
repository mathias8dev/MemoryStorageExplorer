package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory


@Factory
class CurrentPathIsStorageVolumePathUseCase(
    private val storageVolumeFromPathUseCase: StorageVolumeFromPathUseCase
) {

    suspend operator fun invoke(path: String): Boolean = withContext(Dispatchers.IO) {
        storageVolumeFromPathUseCase.invoke(path, true) != null
    }
}
