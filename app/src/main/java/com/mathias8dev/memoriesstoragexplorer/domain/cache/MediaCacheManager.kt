package com.mathias8dev.memoriesstoragexplorer.domain.cache

import com.mathias8dev.memoriesstoragexplorer.domain.models.MediaInfo
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.koin.core.annotation.Single
import timber.log.Timber

/**
 * Central cache manager for media data
 *
 * Provides thread-safe access to the LRU cache with automatic key generation
 * and cache invalidation strategies.
 */
@Single
class MediaCacheManager {
    private val cache = LruMediaCache()
    private val mutex = Mutex()

    /**
     * Get cached media list for a path
     */
    suspend fun get(path: String): List<MediaInfo>? {
        return mutex.withLock {
            val key = generateKey(path)
            cache.get(key)?.data
        }
    }

    /**
     * Get cached media list for a specific query type
     */
    suspend fun get(queryType: QueryType): List<MediaInfo>? {
        return mutex.withLock {
            val key = generateKey(queryType)
            cache.get(key)?.data
        }
    }

    /**
     * Store media list in cache for a path
     */
    suspend fun put(path: String, data: List<MediaInfo>) {
        mutex.withLock {
            val key = generateKey(path)
            val entry = MediaCacheEntry(
                data = data,
                path = path
            )
            cache.put(key, entry)
        }
    }

    /**
     * Store media list in cache for a specific query type
     */
    suspend fun put(queryType: QueryType, data: List<MediaInfo>) {
        mutex.withLock {
            val key = generateKey(queryType)
            val entry = MediaCacheEntry(
                data = data,
                path = queryType.name
            )
            cache.put(key, entry)
        }
    }

    /**
     * Invalidate cache for a specific path
     */
    suspend fun invalidate(path: String) {
        mutex.withLock {
            val key = generateKey(path)
            cache.remove(key)

            // Also invalidate parent and child paths
            cache.invalidatePattern(path)
        }
    }

    /**
     * Invalidate cache for a specific query type
     */
    suspend fun invalidate(queryType: QueryType) {
        mutex.withLock {
            val key = generateKey(queryType)
            cache.remove(key)
        }
    }

    /**
     * Invalidate all cache entries under a directory tree
     */
    suspend fun invalidateTree(rootPath: String) {
        mutex.withLock {
            cache.invalidatePattern(rootPath)
            Timber.d("Invalidated cache tree for: $rootPath")
        }
    }

    /**
     * Clear all cache
     */
    suspend fun clearAll() {
        mutex.withLock {
            cache.clear()
        }
    }

    /**
     * Get cache statistics
     */
    suspend fun getStats(): LruMediaCache.CacheStats {
        return mutex.withLock {
            cache.getStats()
        }
    }

    /**
     * Execute a query with caching
     * If cache exists, return it; otherwise execute the query and cache the result
     */
    suspend fun getOrPut(
        path: String,
        query: suspend () -> List<MediaInfo>
    ): List<MediaInfo> {
        // Try to get from cache first
        get(path)?.let { cachedData ->
            Timber.d("Returning cached data for path: $path")
            return cachedData
        }

        // Cache miss - execute query
        Timber.d("Cache miss - executing query for path: $path")
        val result = query()

        // Store in cache
        put(path, result)

        return result
    }

    /**
     * Execute a query with caching for specific query types
     */
    suspend fun getOrPut(
        queryType: QueryType,
        query: suspend () -> List<MediaInfo>
    ): List<MediaInfo> {
        // Try to get from cache first
        get(queryType)?.let { cachedData ->
            Timber.d("Returning cached data for queryType: $queryType")
            return cachedData
        }

        // Cache miss - execute query
        Timber.d("Cache miss - executing query for queryType: $queryType")
        val result = query()

        // Store in cache
        put(queryType, result)

        return result
    }

    private fun generateKey(path: String): String {
        // Normalize path to avoid duplicate cache entries
        return path.trim().trimEnd('/')
    }

    private fun generateKey(queryType: QueryType): String {
        return "query_type:${queryType.name}"
    }

    /**
     * Enum for different query types to distinguish cache entries
     */
    enum class QueryType {
        ALL_IMAGES,
        ALL_VIDEOS,
        ALL_AUDIOS,
        ALL_DOCUMENTS,
        ALL_ARCHIVES,
        ALL_APKS,
        ALL_MEDIA,
        RECENT_FILES,
        RECYCLE_BIN,
        INSTALLED_APPS
    }
}
