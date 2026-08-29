package com.mutazyounes.prayerathan.audio

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mutazyounes.prayerathan.PrayerAthanApp
import java.time.ZoneId

class AthkarAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as PrayerAthanApp
        if (!AudioSettingsStore(app).athkarEnabled()) {
            val now = app.wallClock.now()
            app.athanController.schedule(app.prayerEngine.day(now), now)
            return
        }
        val now = app.wallClock.now()
        val location = app.prayerEngine.location()
        val day = app.prayerEngine.day(now, location)
        val zone = ZoneId.of(location.timeZoneId)
        if (app.athanController.playback.value != null ||
            !isAthkarWindow(day, now, zone) ||
            isAthanMinute(athanInstants(day), now, zone)
        ) {
            app.athanController.schedule(day, now)
            return
        }
        context.startForegroundService(AthkarService.playIntent(context))
    }

    companion object {
        private const val FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        private const val ACTION = "com.mutazyounes.prayerathan.audio.ATHKAR_ALARM"
        const val EXTRA_HOUR = "hour"

        fun pendingIntent(context: Context, hour: Int): PendingIntent {
            val intent = Intent(context, AthkarAlarmReceiver::class.java).apply {
                action = "$ACTION.$hour"
                putExtra(EXTRA_HOUR, hour)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode(hour),
                intent,
                FLAG,
            )
        }

        internal fun requestCode(hour: Int): Int = 600 + hour.coerceIn(0, 23)
    }
}
