package com.mathias8dev.memoriesstoragexplorer.ui.activities.imageViewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mathias8dev.memoriesstoragexplorer.SettingsProviderViewModel
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.ui.composables.ImageViewerComposable
import com.mathias8dev.memoriesstoragexplorer.ui.theme.MemoriesStorageExplorerTheme
import org.koin.androidx.compose.koinViewModel


class ImageViewerActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val uri = intent.data
        val type = intent.type

        setContent {
            val viewModel: SettingsProviderViewModel = koinViewModel()
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle(AppSettings())

            MemoriesStorageExplorerTheme(
                darkTheme = appSettings.useDarkMode || isSystemInDarkTheme()
            ) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(text = "Preview")
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
                                }
                            )
                        }
                    ) { padding ->

                        if (type != null && type.startsWith("image/")) {
                            ImageViewerComposable(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding),
                                model = uri,
                            )
                        } else {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "An error occured, sorry")
                            }
                        }
                    }
                }
            }
        }
    }
}