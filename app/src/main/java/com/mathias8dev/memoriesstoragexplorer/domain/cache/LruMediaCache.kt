package com.mathias8dev.memoriesstoragexplorer.domain.cache

import android.util.LruCache
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes

/**
 * LRU (Least Recently Used) cache for media lists
 *
 * This cache stores media lists in memory with automatic eviction of least recently used entries
 * when the cache size limit is reached.
 */
class LruMediaCache(
    maxSize: Int = DEFAULT_MAX_SIZE
) {
    private val cache = LruCache<String, MediaCacheEntry>(maxSize)

    // Default TTL: 5 minutes
    private val defaultTtl = 5.minutes.inWholeMilliseconds

    /**
     * Get cached data for a given key if it exists and is not expired
     */
    fun get(key: String): MediaCacheEntry? {
        val entry = cache.get(key)

        if (entry != null) {
            if (entry.isExpired(defaultTtl)) {
                Timber.d("Cache entry expired for key: $key")
                cache.remove(key)
                return null
            }
            Timber.d("Cache hit for key: $key (size: ${entry.data.size})")
            return entry
        }

        Timber.d("Cache miss for key: $key")
        return null
    }

    /**
     * Put data into cache
     */
    fun put(key: String, entry: MediaCacheEntry) {
        cache.put(key, entry)
        Timber.d("Cache stored for key: $key (size: ${entry.data.size})")
    }

    /**
     * Remove a specific entry from cache
     */
    fun remove(key: String) {
        cache.remove(key)
        Timber.d("Cache removed for key: $key")
    }

    /**
     * Clear all cache entries
     */
    fun clear() {
        cache.evictAll()
        Timber.d("Cache cleared")
    }

    /**
     * Invalidate cache entries matching a pattern (e.g., all entries under a path)
     */
    fun invalidatePattern(pattern: String) {
        val keysToRemove = mutableListOf<String>()

        // Collect keys to remove
        cache.snapshot().keys.forEach { key ->
            if (key.startsWith(pattern)) {
                keysToRemove.add(key)
            }
        }

        // Remove them
        keysToRemove.forEach { key ->
            cache.remove(key)
            Timber.d("Cache invalidated for key: $key")
        }
    }

    /**
     * Get current cache size
     */
    fun size(): Int = cache.size()

    /**
     * Get cache statistics for debugging
     */
    fun getStats(): CacheStats {
        val snapshot = cache.snapshot()
        val totalMemoryBytes = calculateMemoryUsage(snapshot.values)

        return CacheStats(
            size = cache.size(),
            maxSize = cache.maxSize(),
            hitCount = cache.hitCount(),
            missCount = cache.missCount(),
            evictionCount = cache.evictionCount(),
            memoryBytes = totalMemoryBytes
        )
    }

    /**
     * Calculates approximate memory usage of cache entries
     */
    private fun calculateMemoryUsage(entries: Collection<MediaCacheEntry>): Long {
        var totalBytes = 0L

        entries.forEach { entry ->
            // Base overhead per entry
            totalBytes += 48 // Object header + references

            // MediaCacheEntry fields
            totalBytes += 8 // timestamp (Long)
            totalBytes += estimateStringMemory(entry.path)

            // MediaInfo list
            totalBytes += 24 // ArrayList overhead
            entry.data.forEach { mediaInfo ->
                // Each MediaInfo object
                totalBytes += 48 // Object header
                totalBytes += 16 // Two Longs (mediaId, size)

                // URI strings
                mediaInfo.contentUri?.toString()?.let { totalBytes += estimateStringMemory(it) }
                mediaInfo.privateContentUri?.toString()?.let { totalBytes += estimateStringMemory(it) }
                mediaInfo.bucketPrivateContentUri?.toString()?.let { totalBytes += estimateStringMemory(it) }

                // String fields
                mediaInfo.name?.let { totalBytes += estimateStringMemory(it) }
                mediaInfo.bucketName?.let { totalBytes += estimateStringMemory(it) }
                mediaInfo.mimeTypeString?.let { totalBytes += estimateStringMemory(it) }
            }
        }

        return totalBytes
    }

    /**
     * Estimates memory used by a String
     * String overhead (24 bytes) + char array (2 bytes per character)
     */
    private fun estimateStringMemory(str: String): Long {
        return 24 + (str.length * 2L)
    }

    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val hitCount: Int,
        val missCount: Int,
        val evictionCount: Int,
        val memoryBytes: Long
    ) {
        val hitRate: Float
            get() = if (hitCount + missCount > 0) {
                hitCount.toFloat() / (hitCount + missCount)
            } else 0f

        val memoryKB: Double
            get() = memoryBytes / 1024.0

        val memoryMB: Double
            get() = memoryBytes / (1024.0 * 1024.0)

        fun formatMemory(): String {
            return when {
                memoryBytes < 1024 -> "$memoryBytes B"
                memoryBytes < 1024 * 1024 -> String.format("%.2f KB", memoryKB)
                else -> String.format("%.2f MB", memoryMB)
            }
        }
    }

    companion object {
        // Default max size: 50 entries
        private const val DEFAULT_MAX_SIZE = 50
    }
}
