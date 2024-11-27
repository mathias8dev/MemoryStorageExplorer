package com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaPlayer

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaPlayer.components.MediaPlayer
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph


@Composable
@Destination
@RootNavGraph
fun VideoScreen(
    uri: Uri
) {
    MediaPlayer(
        modifier = Modifier.fillMaxSize(),
        uri = uri
    )
}