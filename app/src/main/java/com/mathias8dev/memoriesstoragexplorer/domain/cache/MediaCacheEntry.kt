package com.mathias8dev.memoriesstoragexplorer.domain.cache

import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo

/**
 * Cache entry with timestamp for expiration management
 */
data class MediaCacheEntry(
    val data: List<MediaInfo>,
    val timestamp: Long = System.currentTimeMillis(),
    val path: String
) {
    /**
     * Check if the cache entry is expired based on TTL (Time To Live)
     */
    fun isExpired(ttlMillis: Long): Boolean {
        return System.currentTimeMillis() - timestamp > ttlMillis
    }
}
