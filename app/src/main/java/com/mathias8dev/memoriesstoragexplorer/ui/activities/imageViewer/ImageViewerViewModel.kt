package com.mathias8dev.memoriesstoragexplorer.ui.activities.imageViewer

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mathias8dev.memoriesstoragexplorer.domain.useCases.queries.QueryMediaListFromPathUseCase
import com.mathias8dev.memoriesstoragexplorer.ui.utils.mimeData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber

/**
 * ViewModel for ImageViewerActivity with true lazy loading
 * Only loads adjacent images when user swipes
 */
@KoinViewModel
class ImageViewerViewModel(
    private val queryMediaListFromPathUseCase: QueryMediaListFromPathUseCase,
) : ViewModel() {

    private val _imageUris = MutableStateFlow<List<Uri>>(emptyList())
    val imageUris = _imageUris.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex = _currentIndex.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var folderPath: String? = null

    /**
     * Initialize with a single image URI, folder path, and file path for matching
     */
    fun initialize(uri: Uri, folderPath: String?, filePath: String?) {
        Timber.d("=== INIT === URI: $uri")
        Timber.d("=== INIT === Folder: $folderPath")
        Timber.d("=== INIT === FilePath: $filePath")

        // Start with just the current image for instant display
        _imageUris.value = listOf(uri)
        _currentIndex.value = 0
        this.folderPath = folderPath

        // Load all images from folder in background for swipe navigation
        if (folderPath != null && filePath != null) {
            Timber.d("=== INIT === Starting to load images from folder")
            loadImagesFromFolder(folderPath, filePath)
        } else {
            Timber.w("=== INIT === Cannot load images: folderPath=$folderPath, filePath=$filePath")
        }
    }

    /**
     * Update current page index
     */
    fun onPageChanged(newIndex: Int) {
        _currentIndex.value = newIndex
        Timber.d("Page changed to: $newIndex")
    }

    /**
     * Load all images from the folder in background to enable swipe navigation
     */
    private fun loadImagesFromFolder(folderPath: String, currentFilePath: String) {
        viewModelScope.launch {
            if (_isLoading.value) {
                Timber.w("=== LOAD === Already loading, skipping")
                return@launch
            }

            _isLoading.value = true
            Timber.d("=== LOAD === Starting to load from folder: $folderPath")
            Timber.d("=== LOAD === Current file path: $currentFilePath")

            withContext(Dispatchers.IO) {
                try {
                    // Query all media in the folder
                    val mediaList = queryMediaListFromPathUseCase.invoke(folderPath, useCache = true)
                    Timber.d("=== LOAD === Query returned ${mediaList.size} media items")

                    // Filter only images and map to content URIs
                    val imagesWithPaths = mediaList.mapNotNull { mediaInfo ->
                        val file = mediaInfo.privateContentUri?.toFile()
                        val isImage = file?.mimeData?.isImage == true
                        val contentUri = mediaInfo.contentUri

                        Timber.v("=== LOAD === File: ${file?.absolutePath}, isImage: $isImage, contentUri: $contentUri")

                        if (isImage && contentUri != null) {
                            file.absolutePath to contentUri
                        } else null
                    }

                    Timber.d("=== LOAD === Found ${imagesWithPaths.size} images in folder")

                    if (imagesWithPaths.isNotEmpty()) {
                        // Extract URIs and find current image by file path
                        val uris = imagesWithPaths.map { it.second }
                        val currentIndex = imagesWithPaths.indexOfFirst { it.first == currentFilePath }

                        Timber.d("=== LOAD === Current index: $currentIndex (looking for: $currentFilePath)")
                        imagesWithPaths.forEachIndexed { index, pair ->
                            Timber.d("=== LOAD === [$index] ${pair.first}")
                        }

                        withContext(Dispatchers.Main) {
                            _imageUris.value = uris
                            _currentIndex.value = if (currentIndex >= 0) currentIndex else 0
                            Timber.d("=== LOAD === SUCCESS: Loaded ${uris.size} images, current index: ${_currentIndex.value}")
                        }
                    } else {
                        Timber.w("=== LOAD === No images found in folder")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "=== LOAD === ERROR: Failed to load images from folder")
                } finally {
                    _isLoading.value = false
                    Timber.d("=== LOAD === Finished loading")
                }
            }
        }
    }
}
