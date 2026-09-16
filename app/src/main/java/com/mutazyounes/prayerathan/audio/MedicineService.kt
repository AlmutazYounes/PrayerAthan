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

class MedicineService : Service() {

    private val player by lazy { AthanPlayer(this) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> startPlayback()
            ACTION_DEMO -> startDemo()
            ACTION_STOP -> {
                startInForeground(getString(R.string.medicine_playing_title))
                stopPlayback()
            }
            else -> stopPlayback()
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player.stop()
        controller().markMedicineIdle()
        controller().markDemo(null)
        super.onDestroy()
    }

    private fun startPlayback() {
        val app = application as PrayerAthanApp
        val settings = MedicineSettingsStore(this)
        val now = app.wallClock.now()
        val location = app.prayerEngine.location()
        val day = app.prayerEngine.day(now, location)
        val zone = ZoneId.of(location.timeZoneId)
        val voice = settings.voice()
        startInForeground(voice.wallPrimary)
        if (!settings.enabled() ||
            settings.slots().isEmpty() ||
            app.athanController.playback.value != null ||
            isAthanMinute(athanInstants(day), now, zone) ||
            !isMedicineMinute(settings.slots(), now, zone)
        ) {
            app.athanController.schedule(day, now)
            stopPlayback()
            return
        }
        app.athanController.stopAthkar()
        app.athanController.markMedicinePlaying(voice.wallPrimary, voice.wallSecondary, now)
        app.athanController.schedule(day, now)
        player.playRaw(
            resId = voice.rawRes,
            onComplete = { stopPlayback() },
            onError = { stopPlayback() },
        )
    }

    private fun startDemo() {
        val settings = MedicineSettingsStore(this)
        val voice = settings.voice()
        val app = application as PrayerAthanApp
        startInForeground(voice.wallPrimary)
        app.athanController.stopAthkar()
        app.athanController.markIdle()
        app.athanController.markDemo(DEMO_ID)
        app.athanController.markMedicinePlaying(
            voice.wallPrimary,
            voice.wallSecondary,
            app.wallClock.now(),
        )
        player.playRaw(
            resId = voice.rawRes,
            onComplete = { stopPlayback() },
            onError = { stopPlayback() },
        )
    }

    private fun stopPlayback() {
        player.stop()
        controller().markMedicineIdle()
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
            4,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stop = PendingIntent.getService(
            this,
            5,
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
            .setContentTitle(getString(R.string.medicine_playing_title))
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
            getString(R.string.medicine_channel_name),
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
        const val ACTION_PLAY = "com.mutazyounes.prayerathan.audio.MEDICINE_PLAY"
        const val ACTION_DEMO = "com.mutazyounes.prayerathan.audio.MEDICINE_DEMO"
        const val ACTION_STOP = "com.mutazyounes.prayerathan.audio.MEDICINE_STOP"
        const val DEMO_ID = "medicine:preview"
        private const val CHANNEL_ID = "medicine_playback"
        private const val NOTIFICATION_ID = 43

        fun playIntent(context: Context): Intent {
            return Intent(context, MedicineService::class.java).apply {
                action = ACTION_PLAY
            }
        }

        fun demoIntent(context: Context): Intent {
            return Intent(context, MedicineService::class.java).apply {
                action = ACTION_DEMO
            }
        }

        fun stopIntent(context: Context): Intent {
            return Intent(context, MedicineService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }
}
