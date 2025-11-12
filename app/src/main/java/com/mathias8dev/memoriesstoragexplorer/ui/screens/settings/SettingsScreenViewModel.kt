package com.mathias8dev.memoriesstoragexplorer.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathias8dev.memoriesstoragexplorer.data.repositories.AppSettingsRepository
import com.mathias8dev.memoriesstoragexplorer.domain.cache.LruMediaCache
import com.mathias8dev.memoriesstoragexplorer.domain.cache.MediaCacheManager
import com.mathias8dev.memoriesstoragexplorer.domain.services.AppSettingsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber


@KoinViewModel
class SettingsScreenViewModel(
    appSettingsRepository: AppSettingsRepository,
    private val cacheManager: MediaCacheManager
) : ViewModel(), AppSettingsService by appSettingsRepository {

    private val _cacheStats = MutableStateFlow<LruMediaCache.CacheStats?>(null)
    val cacheStats = _cacheStats.asStateFlow()

    init {
        loadCacheStats()
    }

    /**
     * Load cache statistics
     */
    fun loadCacheStats() {
        viewModelScope.launch {
            _cacheStats.value = cacheManager.getStats()
        }
    }

    /**
     * Clear all cache
     */
    fun clearAllCache() {
        viewModelScope.launch {
            cacheManager.clearAll()
            loadCacheStats()
            Timber.i("Cache cleared from settings")
        }
    }
}