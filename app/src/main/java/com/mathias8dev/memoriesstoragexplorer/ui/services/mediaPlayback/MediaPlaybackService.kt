package com.mathias8dev.memoriesstoragexplorer.ui.services.mediaPlayback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import androidx.media3.ui.PlayerNotificationManager
import timber.log.Timber

class MediaPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var player: ExoPlayer? = null
    private var playerNotificationManager: PlayerNotificationManager? = null
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "media_playback_channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Media Playback"
    }

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("MediaPlaybackService onCreate")
        createNotificationChannel()
    }

    @OptIn(UnstableApi::class)
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    fun setPlayer(exoPlayer: ExoPlayer) {
        if (player == exoPlayer) return

        // Release old player if exists
        releasePlayer()

        player = exoPlayer

        // Create media session
        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setCallback(object : MediaSession.Callback {
                override fun onConnect(
                    session: MediaSession,
                    controller: MediaSession.ControllerInfo
                ): MediaSession.ConnectionResult {
                    return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                        .setAvailableSessionCommands(MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS)
                        .setAvailablePlayerCommands(MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS)
                        .build()
                }
            })
            .build()

        setupNotificationManager()
    }

    @OptIn(UnstableApi::class)
    private fun setupNotificationManager() {
        val player = this.player ?: return

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(MediaDescriptionAdapter(this))
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    Timber.d("Notification cancelled: dismissedByUser=$dismissedByUser")
                    stopSelf()
                }

                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    Timber.d("Notification posted: ongoing=$ongoing")
                    if (ongoing) {
                        startForeground(notificationId, notification)
                    } else {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    }
                }
            })
            .build()
            .apply {
                setPlayer(player)
                setUsePlayPauseActions(true)
                setUseRewindAction(true)
                setUseFastForwardAction(true)
                setUseStopAction(false)
            }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification for media playback controls"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun releasePlayer() {
        playerNotificationManager?.setPlayer(null)
        playerNotificationManager = null
        mediaSession?.release()
        mediaSession = null
        player = null
    }

    override fun onDestroy() {
        Timber.d("MediaPlaybackService onDestroy")
        releasePlayer()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = this.player
        if (player != null && !player.playWhenReady) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }
}
