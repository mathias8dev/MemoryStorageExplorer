package com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaPlayer.components


import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import timber.log.Timber

@OptIn(UnstableApi::class)
@Composable
fun MediaPlayer(
    modifier: Modifier = Modifier,
    uri: Uri
) {
    val context = LocalContext.current

    var currentPosition by rememberSaveable { mutableLongStateOf(0L) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .build()
            .apply {
                val defaultDataSourceFactory = DefaultDataSource.Factory(context)
                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                    context,
                    defaultDataSourceFactory
                )
                val source = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))
                setMediaSource(source)
                prepare()

                playWhenReady = true
                videoScalingMode = C.VIDEO_SCALING_MODE_DEFAULT
                repeatMode = Player.REPEAT_MODE_OFF
            }
    }




    AndroidView(
        factory = {
            PlayerView(context).apply {
                useController = true
                player = exoPlayer
                keepScreenOn = true
                artworkDisplayMode = PlayerView.ARTWORK_DISPLAY_MODE_FILL
                layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            }
        },
        modifier = modifier
    )


    DisposableEffect(Unit) {
        Timber.d("OnSeek to $currentPosition")
        exoPlayer.seekTo(currentPosition)
        onDispose {
            Timber.d("OnDispose")
            exoPlayer.release()
            currentPosition = exoPlayer.currentPosition
            Timber.d("The current position is $currentPosition")
        }
    }

}