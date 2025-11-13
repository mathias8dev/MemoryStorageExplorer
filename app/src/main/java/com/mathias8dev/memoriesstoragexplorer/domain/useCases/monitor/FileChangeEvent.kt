package com.mathias8dev.memoriesstoragexplorer.domain.useCases.monitor

/**
 * Events emitted when file system changes are detected
 */
sealed class FileChangeEvent {
    /**
     * A file or directory was created
     */
    data class Created(val path: String) : FileChangeEvent()

    /**
     * A file or directory was deleted
     */
    data class Deleted(val path: String) : FileChangeEvent()

    /**
     * A file was modified
     */
    data class Modified(val path: String) : FileChangeEvent()

    /**
     * A file or directory was moved/renamed
     */
    data class MovedFrom(val path: String) : FileChangeEvent()
    data class MovedTo(val path: String) : FileChangeEvent()

    /**
     * MediaStore content changed (images, videos, audio)
     */
    data class MediaStoreChanged(val type: MediaType) : FileChangeEvent() {
        enum class MediaType {
            IMAGES,
            VIDEOS,
            AUDIO,
            FILES, // Generic files
            DOWNLOADS
        }
    }

    /**
     * Batch of changes detected in a directory
     */
    data class DirectoryChanged(val path: String, val changeCount: Int) : FileChangeEvent()
}
