package com.mathias8dev.memoriesstoragexplorer.domain.observers

import android.os.FileObserver
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.monitor.FileChangeEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import java.io.File

/**
 * Observes file system changes in a specific directory using FileObserver
 * Monitors CREATE, DELETE, MODIFY, MOVE events
 */
class DirectoryObserver(
    private val directoryPath: String
) {
    private val _events = MutableSharedFlow<FileChangeEvent>(replay = 0)
    val events: SharedFlow<FileChangeEvent> = _events.asSharedFlow()

    private var fileObserver: FileObserver? = null
    private var changeCount = 0
    private var lastEmitTime = 0L

    // Debounce: emit batch events every 500ms max
    private val debounceMillis = 500L

    /**
     * Starts observing the directory for file changes
     */
    fun startObserving() {
        if (fileObserver != null) {
            Timber.w("DirectoryObserver already observing: $directoryPath")
            return
        }

        val directory = File(directoryPath)
        if (!directory.exists() || !directory.isDirectory) {
            Timber.e("Cannot observe non-existent or non-directory path: $directoryPath")
            return
        }

        // API compatibility: Use appropriate constructor
        fileObserver = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // API 29+: Use File-based constructor
            object : FileObserver(directory, ALL_EVENTS) {
                override fun onEvent(event: Int, path: String?) {
                    handleFileEvent(event, path)
                }
            }
        } else {
            // API 24-28: Use String-based constructor
            @Suppress("DEPRECATION")
            object : FileObserver(directoryPath, ALL_EVENTS) {
                override fun onEvent(event: Int, path: String?) {
                    handleFileEvent(event, path)
                }
            }
        }

        fileObserver?.startWatching()
        Timber.i("DirectoryObserver started for: $directoryPath")
    }

    /**
     * Stops observing the directory
     */
    fun stopObserving() {
        fileObserver?.stopWatching()
        fileObserver = null
        Timber.i("DirectoryObserver stopped for: $directoryPath")
    }

    private fun handleFileEvent(event: Int, fileName: String?) {
        val fullPath = if (fileName != null) {
            "$directoryPath${File.separator}$fileName"
        } else {
            directoryPath
        }

        val fileChangeEvent = when (event and ALL_EVENTS) {
            FileObserver.CREATE -> {
                Timber.d("File created: $fullPath")
                FileChangeEvent.Created(fullPath)
            }
            FileObserver.DELETE, FileObserver.DELETE_SELF -> {
                Timber.d("File deleted: $fullPath")
                FileChangeEvent.Deleted(fullPath)
            }
            FileObserver.MODIFY -> {
                Timber.d("File modified: $fullPath")
                FileChangeEvent.Modified(fullPath)
            }
            FileObserver.MOVED_FROM -> {
                Timber.d("File moved from: $fullPath")
                FileChangeEvent.MovedFrom(fullPath)
            }
            FileObserver.MOVED_TO -> {
                Timber.d("File moved to: $fullPath")
                FileChangeEvent.MovedTo(fullPath)
            }
            FileObserver.CLOSE_WRITE -> {
                // File closed after writing - indicates completion
                Timber.d("File write completed: $fullPath")
                FileChangeEvent.Modified(fullPath)
            }
            else -> null
        }

        // Emit individual events or batch them
        if (fileChangeEvent != null) {
            changeCount++
            val now = System.currentTimeMillis()

            if (now - lastEmitTime > debounceMillis) {
                // Emit batched event for better performance
                if (changeCount > 1) {
                    _events.tryEmit(FileChangeEvent.DirectoryChanged(directoryPath, changeCount))
                    Timber.d("Emitted batch event: $changeCount changes in $directoryPath")
                } else {
                    _events.tryEmit(fileChangeEvent)
                }
                changeCount = 0
                lastEmitTime = now
            } else if (changeCount == 1) {
                // First event in batch - emit immediately for responsiveness
                _events.tryEmit(fileChangeEvent)
            }
        }
    }

    companion object {
        // All relevant events we want to monitor
        private val ALL_EVENTS = FileObserver.CREATE or FileObserver.DELETE or FileObserver.DELETE_SELF or FileObserver.MODIFY or
                FileObserver.MOVED_FROM or FileObserver.MOVED_TO or FileObserver.CLOSE_WRITE
    }
}
