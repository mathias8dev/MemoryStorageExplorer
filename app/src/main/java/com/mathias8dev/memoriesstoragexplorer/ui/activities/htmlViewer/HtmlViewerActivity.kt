package com.mathias8dev.memoriesstoragexplorer.ui.activities.htmlViewer

import android.net.Uri
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mathias8dev.memoriesstoragexplorer.SettingsProviderViewModel
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.ui.activities.htmlViewer.components.HtmlRenderer
import com.mathias8dev.memoriesstoragexplorer.ui.theme.MemoriesStorageExplorerTheme
import org.koin.androidx.compose.koinViewModel

class HtmlViewerActivity : ComponentActivity() {


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        val htmlContent = uri?.let { loadHtmlContent(it) }


        enableEdgeToEdge()
        setContent {

            val viewModel: SettingsProviderViewModel = koinViewModel()
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle(AppSettings())

            MemoriesStorageExplorerTheme(
                darkTheme = appSettings.useDarkMode || isSystemInDarkTheme()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize()
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
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(it)
                                .padding(16.dp)
                        ) {
                            if (htmlContent != null) {
                                HtmlRenderer(
                                    modifier = Modifier.fillMaxSize(),
                                    text = htmlContent
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

    private fun loadHtmlContent(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}