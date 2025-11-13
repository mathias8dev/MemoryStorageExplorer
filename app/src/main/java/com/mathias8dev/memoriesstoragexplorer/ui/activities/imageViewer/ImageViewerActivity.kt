package com.mathias8dev.memoriesstoragexplorer.ui.activities.imageViewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mathias8dev.memoriesstoragexplorer.SettingsProviderViewModel
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.ui.composables.viewer.ImageViewerComposable
import com.mathias8dev.memoriesstoragexplorer.ui.theme.MemoriesStorageExplorerTheme
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber


class ImageViewerActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uri = intent.data
        val type = intent.type
        val folderPath = intent.getStringExtra("folder_path")
        val filePath = intent.getStringExtra("file_path")

        setContent {
            val settingsViewModel: SettingsProviderViewModel = koinViewModel()
            val imageViewerViewModel: ImageViewerViewModel = koinViewModel()
            val appSettings by settingsViewModel.appSettings.collectAsStateWithLifecycle(AppSettings())

            val imageUris by imageViewerViewModel.imageUris.collectAsStateWithLifecycle()
            val currentIndex by imageViewerViewModel.currentIndex.collectAsStateWithLifecycle()
            val isLoading by imageViewerViewModel.isLoading.collectAsStateWithLifecycle()

            var isImageZoomed by remember { mutableStateOf(false) }
            var showTopBar by remember { mutableStateOf(true) }

            // Initialize ViewModel with the initial image
            LaunchedEffect(uri, folderPath, filePath) {
                if (uri != null) {
                    imageViewerViewModel.initialize(uri, folderPath, filePath)
                }
            }

            // Create pager state that updates when images are loaded
            val pagerState = rememberPagerState(
                initialPage = currentIndex,
                pageCount = { imageUris.size }
            )

            // Sync pager state with current index from ViewModel
            LaunchedEffect(currentIndex) {
                if (pagerState.currentPage != currentIndex && currentIndex < imageUris.size) {
                    pagerState.scrollToPage(currentIndex)
                }
            }

            // Reset zoom state when page changes
            LaunchedEffect(pagerState.currentPage) {
                isImageZoomed = false
                imageViewerViewModel.onPageChanged(pagerState.currentPage)
            }

            MemoriesStorageExplorerTheme(
                darkTheme = appSettings.useDarkMode || isSystemInDarkTheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold { padding ->

                        Timber.d("ImageUris: $imageUris")
                        if (type != null && type.startsWith("image/")) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                if (imageUris.isNotEmpty()) {
                                    HorizontalPager(
                                        state = pagerState,
                                        modifier = Modifier.fillMaxSize(),
                                        // Allow pager scroll only when images are not zoomed
                                        userScrollEnabled = !isImageZoomed,
                                        pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                                            state = pagerState,
                                            orientation = androidx.compose.foundation.gestures.Orientation.Horizontal
                                        )
                                    ) { page ->
                                        ImageViewerComposable(
                                            modifier = Modifier.fillMaxSize(),
                                            model = imageUris[page],
                                            onZoomChanged = { zoomed ->
                                                isImageZoomed = zoomed
                                                Timber.d("Zoom changed to $zoomed")
                                            },
                                            onSingleTap = {
                                                // Toggle topbar on single tap
                                                showTopBar = !showTopBar
                                            }
                                        )
                                    }
                                }

                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(16.dp)
                                    )
                                }

                                // Animated TopBar
                                AnimatedVisibility(
                                    visible = showTopBar,
                                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                                    modifier = Modifier.align(Alignment.TopCenter)
                                ) {
                                    TopAppBar(
                                        title = {
                                            Text(
                                                text = if (imageUris.isNotEmpty()) {
                                                    "${pagerState.currentPage + 1} / ${imageUris.size}"
                                                } else {
                                                    "Preview"
                                                }
                                            )
                                        },
                                        navigationIcon = {
                                            IconButton(
                                                onClick = {
                                                    finish()
                                                },
                                                content = {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        contentDescription = "Back"
                                                    )
                                                }
                                            )
                                        },
                                        colors = TopAppBarDefaults.topAppBarColors(
                                            containerColor = Color.Black.copy(alpha = 0.7f),
                                            titleContentColor = Color.White,
                                            navigationIconContentColor = Color.White
                                        )
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "An error occurred, sorry")
                            }
                        }
                    }
                }
            }
        }
    }
}