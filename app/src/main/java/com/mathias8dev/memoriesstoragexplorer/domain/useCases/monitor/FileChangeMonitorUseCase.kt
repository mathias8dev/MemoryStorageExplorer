package com.mathias8dev.memoriesstoragexplorer.domain.useCases.monitor

import android.content.Context
import com.mathias8dev.memoriesstoragexplorer.domain.observers.DirectoryObserver
import com.mathias8dev.memoriesstoragexplorer.domain.observers.MediaStoreObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import timber.log.Timber

/**
 * UseCase that monitors file system changes
 * Combines MediaStore ContentObserver (for system-level media changes)
 * and FileObserver (for specific directory monitoring)
 */
@Single
class FileChangeMonitorUseCase(
    private val context: Context
) {
    private val _events = MutableSharedFlow<FileChangeEvent>(replay = 0)
    val events: SharedFlow<FileChangeEvent> = _events.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // MediaStore observer for system-level changes
    private var mediaStoreObserver: MediaStoreObserver? = null

    // Map of directory paths to their observers
    private val directoryObservers = mutableMapOf<String, DirectoryObserver>()

    /**
     * Starts monitoring MediaStore changes (images, videos, audio)
     * This should be called when the app starts
     */
    fun startMediaStoreMonitoring() {
        if (mediaStoreObserver != null) {
            Timber.w("MediaStore monitoring already started")
            return
        }

        mediaStoreObserver = MediaStoreObserver(context).apply {
            startObserving()

            // Forward events to main flow
            scope.launch {
                events.collect { event ->
                    _events.emit(event)
                }
            }
        }

        Timber.i("MediaStore monitoring started")
    }

    /**
     * Stops monitoring MediaStore changes
     */
    fun stopMediaStoreMonitoring() {
        mediaStoreObserver?.stopObserving()
        mediaStoreObserver = null
        Timber.i("MediaStore monitoring stopped")
    }

    /**
     * Starts monitoring a specific directory
     * Call this when user navigates to a folder
     */
    fun startDirectoryMonitoring(path: String) {
        if (directoryObservers.containsKey(path)) {
            Timber.d("Already monitoring directory: $path")
            return
        }

        val observer = DirectoryObserver(path).apply {
            startObserving()

            // Forward events to main flow
            scope.launch {
                events.collect { event ->
                    _events.emit(event)
                }
            }
        }

        directoryObservers[path] = observer
        Timber.i("Started monitoring directory: $path")
    }

    /**
     * Stops monitoring a specific directory
     * Call this when user leaves a folder or when cleaning up
     */
    fun stopDirectoryMonitoring(path: String) {
        directoryObservers.remove(path)?.apply {
            stopObserving()
            Timber.i("Stopped monitoring directory: $path")
        }
    }

    /**
     * Stops monitoring all directories
     */
    fun stopAllDirectoryMonitoring() {
        directoryObservers.values.forEach { it.stopObserving() }
        directoryObservers.clear()
        Timber.i("Stopped monitoring all directories")
    }

    /**
     * Gets list of currently monitored directories
     */
    fun getMonitoredDirectories(): List<String> {
        return directoryObservers.keys.toList()
    }

    /**
     * Cleanup - stops all monitoring
     */
    fun cleanup() {
        stopMediaStoreMonitoring()
        stopAllDirectoryMonitoring()
        Timber.i("FileChangeMonitor cleaned up")
    }
}
