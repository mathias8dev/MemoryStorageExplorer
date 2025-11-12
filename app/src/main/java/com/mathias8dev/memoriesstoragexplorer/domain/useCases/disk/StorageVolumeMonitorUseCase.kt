package com.mathias8dev.memoriesstoragexplorer.domain.useCases.disk

import android.content.Context
import com.mathias8dev.memoriesstoragexplorer.domain.receivers.StorageVolumeReceiver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single
import timber.log.Timber

/**
 * Events emitted when storage volumes are mounted or unmounted
 */
sealed class StorageVolumeEvent {
    data class Mounted(val path: String?) : StorageVolumeEvent()
    data class Unmounted(val path: String?) : StorageVolumeEvent()
    data class Ejected(val path: String?) : StorageVolumeEvent()
    data class Removed(val path: String?) : StorageVolumeEvent()
}

/**
 * UseCase that monitors storage volume mount/unmount events
 */
@Single
class StorageVolumeMonitorUseCase(
    private val context: Context
) {
    private val _events = MutableSharedFlow<StorageVolumeEvent>(replay = 0)
    val events: SharedFlow<StorageVolumeEvent> = _events.asSharedFlow()

    private var receiver: StorageVolumeReceiver? = null

    /**
     * Starts monitoring storage volume events
     */
    fun startMonitoring() {
        if (receiver != null) {
            Timber.w("StorageVolumeMonitor already monitoring")
            return
        }

        receiver = StorageVolumeReceiver(
            onStorageMounted = { path ->
                Timber.d("Emitting Mounted event for path: $path")
                _events.tryEmit(StorageVolumeEvent.Mounted(path))
            },
            onStorageUnmounted = { path ->
                Timber.d("Emitting Unmounted event for path: $path")
                _events.tryEmit(StorageVolumeEvent.Unmounted(path))
            },
            onStorageEjected = { path ->
                Timber.d("Emitting Ejected event for path: $path")
                _events.tryEmit(StorageVolumeEvent.Ejected(path))
            },
            onStorageRemoved = { path ->
                Timber.d("Emitting Removed event for path: $path")
                _events.tryEmit(StorageVolumeEvent.Removed(path))
            }
        )

        StorageVolumeReceiver.register(context, receiver!!)
        Timber.i("StorageVolumeMonitor started")
    }

    /**
     * Stops monitoring storage volume events
     */
    fun stopMonitoring() {
        receiver?.let {
            StorageVolumeReceiver.unregister(context, it)
            receiver = null
            Timber.i("StorageVolumeMonitor stopped")
        }
    }
}
