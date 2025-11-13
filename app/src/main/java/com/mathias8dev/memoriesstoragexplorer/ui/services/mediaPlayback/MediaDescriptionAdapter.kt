package com.mathias8dev.memoriesstoragexplorer.ui.services.mediaPlayback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerNotificationManager
import com.mathias8dev.memoriesstoragexplorer.ui.activities.mediaPlayer.MediaPlayerActivity

@OptIn(UnstableApi::class)
class MediaDescriptionAdapter(
    private val context: Context
) : PlayerNotificationManager.MediaDescriptionAdapter {

    override fun getCurrentContentTitle(player: Player): CharSequence {
        val mediaItem = player.currentMediaItem
        return mediaItem?.mediaMetadata?.title
            ?: mediaItem?.mediaMetadata?.displayTitle
            ?: "Unknown Title"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent? {
        val intent = Intent(context, MediaPlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            setDataAndType(
                player.currentMediaItem?.localConfiguration?.uri,
                if (player.currentTracks.groups.any { it.type == androidx.media3.common.C.TRACK_TYPE_VIDEO }) "video/*" else "audio/*"
            )
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun getCurrentContentText(player: Player): CharSequence? {
        val mediaItem = player.currentMediaItem
        return mediaItem?.mediaMetadata?.artist
            ?: mediaItem?.mediaMetadata?.albumArtist
    }

    override fun getCurrentLargeIcon(
        player: Player,
        callback: PlayerNotificationManager.BitmapCallback
    ): Bitmap? {
        // You can load artwork here if available
        return null
    }
}
