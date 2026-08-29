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
import com.mutazyounes.prayerathan.engine.PrayerName

class AthanService : Service() {

    private val player by lazy { AthanPlayer(this) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> startPlayback(intent)
            ACTION_DEMO -> startDemo(intent)
            ACTION_STOP -> {
                val prayer = controller().playback.value?.prayer ?: PrayerName.DHUHR
                startInForeground(prayer)
                stopPlayback()
            }
            else -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player.stop()
        controller().markIdle()
        controller().markDemo(null)
        super.onDestroy()
    }

    private fun startPlayback(intent: Intent) {
        val prayer = prayerFrom(intent)
        if (prayer == null || prayer == PrayerName.SUNRISE) {
            startInForeground(PrayerName.DHUHR)
            stopPlayback()
            return
        }
        startInForeground(prayer)
        val app = application as PrayerAthanApp
        val now = app.wallClock.now()
        app.athanController.stopAthkar()
        app.athanController.markDemo(null)
        app.athanController.markPlaying(prayer, now)
        app.athanController.schedule(app.prayerEngine.day(now), now)
        player.play(
            prayer = prayer,
            onComplete = { stopPlayback() },
            onError = { stopPlayback() },
        )
    }

    private fun startDemo(intent: Intent) {
        val soundId = intent.getStringExtra(EXTRA_SOUND_ID)
        val choice = soundId?.let { AthanCatalog.byId(it) }
        if (choice == null) {
            startInForeground(PrayerName.DHUHR)
            stopPlayback()
            return
        }
        startInForeground(PrayerName.DHUHR)
        val app = application as PrayerAthanApp
        app.athanController.stopAthkar()
        app.athanController.markIdle()
        app.athanController.markDemo(choice.id)
        player.playRaw(
            resId = choice.rawRes,
            onComplete = { stopPlayback() },
            onError = { stopPlayback() },
        )
    }

    private fun stopPlayback() {
        player.stop()
        controller().markIdle()
        controller().markDemo(null)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startInForeground(prayer: PrayerName) {
        ensureChannel()
        val notification = buildNotification(prayer)
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

    private fun buildNotification(prayer: PrayerName): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this,
            1,
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
            .setContentTitle(getString(R.string.athan_playing_title))
            .setContentText(prayer.name)
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
            getString(R.string.athan_channel_name),
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
        const val ACTION_PLAY = "com.mutazyounes.prayerathan.audio.PLAY"
        const val ACTION_DEMO = "com.mutazyounes.prayerathan.audio.DEMO"
        const val ACTION_STOP = "com.mutazyounes.prayerathan.audio.STOP"
        const val EXTRA_PRAYER = "prayer"
        const val EXTRA_SOUND_ID = "sound_id"
        private const val CHANNEL_ID = "athan_playback"
        private const val NOTIFICATION_ID = 41

        fun playIntent(context: Context, prayer: PrayerName): Intent {
            return Intent(context, AthanService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_PRAYER, prayer.name)
            }
        }

        fun demoIntent(context: Context, soundId: String): Intent {
            return Intent(context, AthanService::class.java).apply {
                action = ACTION_DEMO
                putExtra(EXTRA_SOUND_ID, soundId)
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, AthanService::class.java).apply {
                action = ACTION_STOP
            }
        }

        private fun prayerFrom(intent: Intent): PrayerName? {
            val raw = intent.getStringExtra(EXTRA_PRAYER) ?: return null
            return runCatching { PrayerName.valueOf(raw) }.getOrNull()
        }
    }
}
