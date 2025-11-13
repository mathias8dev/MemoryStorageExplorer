package com.mathias8dev.memoriesstoragexplorer.domain.observers

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.monitor.FileChangeEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber

/**
 * Observes MediaStore changes for images, videos, and audio files
 * This provides system-level notification when media files are added, removed, or modified
 */
class MediaStoreObserver(
    private val context: Context
) {
    private val _events = MutableSharedFlow<FileChangeEvent>(replay = 0)
    val events: SharedFlow<FileChangeEvent> = _events.asSharedFlow()

    private val handler = Handler(Looper.getMainLooper())

    private val imagesObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Timber.d("MediaStore Images changed: $uri")
            _events.tryEmit(FileChangeEvent.MediaStoreChanged(FileChangeEvent.MediaStoreChanged.MediaType.IMAGES))
        }
    }

    private val videosObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Timber.d("MediaStore Videos changed: $uri")
            _events.tryEmit(FileChangeEvent.MediaStoreChanged(FileChangeEvent.MediaStoreChanged.MediaType.VIDEOS))
        }
    }

    private val audioObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Timber.d("MediaStore Audio changed: $uri")
            _events.tryEmit(FileChangeEvent.MediaStoreChanged(FileChangeEvent.MediaStoreChanged.MediaType.AUDIO))
        }
    }

    private val filesObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Timber.d("MediaStore Files changed: $uri")
            _events.tryEmit(FileChangeEvent.MediaStoreChanged(FileChangeEvent.MediaStoreChanged.MediaType.FILES))
        }
    }

    private val downloadsObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Timber.d("MediaStore Downloads changed: $uri")
            _events.tryEmit(FileChangeEvent.MediaStoreChanged(FileChangeEvent.MediaStoreChanged.MediaType.DOWNLOADS))
        }
    }

    private var isRegistered = false

    /**
     * Starts observing MediaStore changes
     */
    fun startObserving() {
        if (isRegistered) {
            Timber.w("MediaStoreObserver already observing")
            return
        }

        val contentResolver = context.contentResolver

        // Observe images
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            imagesObserver
        )

        // Observe videos
        contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            videosObserver
        )

        // Observe audio
        contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            audioObserver
        )

        // Observe files (Android 10+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            contentResolver.registerContentObserver(
                MediaStore.Files.getContentUri("external"),
                true,
                filesObserver
            )

            contentResolver.registerContentObserver(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                true,
                downloadsObserver
            )
        }

        isRegistered = true
        Timber.i("MediaStoreObserver started")
    }

    /**
     * Stops observing MediaStore changes
     */
    fun stopObserving() {
        if (!isRegistered) return

        val contentResolver = context.contentResolver
        contentResolver.unregisterContentObserver(imagesObserver)
        contentResolver.unregisterContentObserver(videosObserver)
        contentResolver.unregisterContentObserver(audioObserver)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            contentResolver.unregisterContentObserver(filesObserver)
            contentResolver.unregisterContentObserver(downloadsObserver)
        }

        isRegistered = false
        Timber.i("MediaStoreObserver stopped")
    }
}
