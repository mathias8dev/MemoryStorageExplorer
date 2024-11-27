package com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaPlayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.mathias8dev.memoriesstoragexplorer.SettingsProviderViewModel
import com.mathias8dev.memoriesstoragexplorer.domain.models.AppSettings
import com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaPlayer.components.MediaPlayer
import com.mathias8dev.memoriesstoragexplorer.ui.theme.MemoriesStorageExplorerTheme
import org.koin.androidx.compose.koinViewModel

class MediaPlayerActivity : ComponentActivity() {


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uri = intent.data
        val type = intent.type



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
                    if (uri != null && (type?.startsWith("video/") == true || type?.startsWith("audio/") == true)) {
                        MediaPlayer(
                            modifier = Modifier.fillMaxSize(),
                            uri = uri
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