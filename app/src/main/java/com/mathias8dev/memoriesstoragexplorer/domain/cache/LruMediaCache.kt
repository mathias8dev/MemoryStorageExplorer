package com.mathias8dev.memoriesstoragexplorer.domain.cache

import android.util.LruCache
import androidx.annotation.VisibleForTesting
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
    @VisibleForTesting
    fun getStats(): CacheStats {
        return CacheStats(
            size = cache.size(),
            maxSize = cache.maxSize(),
            hitCount = cache.hitCount(),
            missCount = cache.missCount(),
            evictionCount = cache.evictionCount()
        )
    }

    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val hitCount: Int,
        val missCount: Int,
        val evictionCount: Int
    ) {
        val hitRate: Float
            get() = if (hitCount + missCount > 0) {
                hitCount.toFloat() / (hitCount + missCount)
            } else 0f
    }

    companion object {
        // Default max size: 50 entries
        private const val DEFAULT_MAX_SIZE = 50
    }
}
