package com.mutazyounes.prayerathan.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import com.mutazyounes.prayerathan.MainActivity
import com.mutazyounes.prayerathan.PrayerAthanApp
import com.mutazyounes.prayerathan.R
import java.time.ZoneId

class AthkarService : Service() {

    private val player by lazy { AthanPlayer(this) }
    private val rotation by lazy { AthkarRotation(this) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> startPlayback()
            ACTION_DEMO -> startDemo(intent)
            ACTION_STOP -> {
                startInForeground(getString(R.string.athkar_playing_title))
                stopPlayback()
            }
            else -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player.stop()
        controller().markAthkarIdle()
        controller().markDemo(null)
        super.onDestroy()
    }

    private fun startPlayback() {
        val app = application as PrayerAthanApp
        val now = app.wallClock.now()
        val location = app.prayerEngine.location()
        val day = app.prayerEngine.day(now, location)
        val zone = ZoneId.of(location.timeZoneId)
        startInForeground(getString(R.string.athkar_playing_title))
        if (!AudioSettingsStore(this).athkarEnabled() ||
            app.athanController.playback.value != null ||
            !isAthkarWindow(day, now, zone) ||
            isAthanMinute(athanInstants(day), now, zone)
        ) {
            app.athanController.schedule(day, now)
            stopPlayback()
            return
        }
        val clip = rotation.next()
        app.athanController.markAthkarPlaying(clip.caption, now)
        app.athanController.schedule(day, now)
        startInForeground(clip.caption)
        player.playRaw(
            resId = clip.rawRes,
            onComplete = { stopPlayback() },
            onError = { stopPlayback() },
        )
    }

    private fun startDemo(intent: Intent) {
        val name = intent.getStringExtra(EXTRA_CLIP)
        val clip = name?.let { runCatching { AthkarClip.valueOf(it) }.getOrNull() }
        if (clip == null) {
            startInForeground(getString(R.string.athkar_playing_title))
            stopPlayback()
            return
        }
        val app = application as PrayerAthanApp
        startInForeground(clip.caption)
        app.athanController.markDemo(demoId(clip))
        app.athanController.markAthkarPlaying(clip.caption, app.wallClock.now())
        player.playRaw(
            resId = clip.rawRes,
            onComplete = { stopPlayback() },
            onError = { stopPlayback() },
        )
    }

    private fun stopPlayback() {
        player.stop()
        controller().markAthkarIdle()
        controller().markDemo(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startInForeground(text: String) {
        ensureChannel()
        val notification = buildNotification(text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(text: String): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            2,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this,
            3,
            stopIntent(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopAction = Notification.Action.Builder(
            Icon.createWithResource(this, R.drawable.ic_athan),
            getString(R.string.athan_stop),
            stop,
        ).build()
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_athan)
            .setContentTitle(getString(R.string.athkar_playing_title))
            .setContentText(text)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(stopAction)
            .build()
    }

    private fun ensureChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.athkar_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        )
        channel.setSound(null, null)
        channel.enableVibration(false)
        channel.setShowBadge(false)
        manager.createNotificationChannel(channel)
    }

    private fun controller(): DefaultAthanController {
        return (application as PrayerAthanApp).athanController
    }

    companion object {
        const val ACTION_PLAY = "com.mutazyounes.prayerathan.audio.ATHKAR_PLAY"
        const val ACTION_DEMO = "com.mutazyounes.prayerathan.audio.ATHKAR_DEMO"
        const val ACTION_STOP = "com.mutazyounes.prayerathan.audio.ATHKAR_STOP"
        const val EXTRA_CLIP = "clip"
        private const val CHANNEL_ID = "athkar_playback"
        private const val NOTIFICATION_ID = 42

        fun playIntent(context: Context): Intent {
            return Intent(context, AthkarService::class.java).apply {
                action = ACTION_PLAY
            }
        }

        fun demoIntent(context: Context, clip: AthkarClip): Intent {
            return Intent(context, AthkarService::class.java).apply {
                action = ACTION_DEMO
                putExtra(EXTRA_CLIP, clip.name)
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, AthkarService::class.java).apply {
                action = ACTION_STOP
            }
        }

        fun demoId(clip: AthkarClip): String = "athkar:${clip.name}"
    }
}
