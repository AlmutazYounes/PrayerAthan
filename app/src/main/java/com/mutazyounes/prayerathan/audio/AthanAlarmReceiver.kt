package com.mutazyounes.prayerathan.audio

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mutazyounes.prayerathan.engine.PrayerName

class AthanAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayer = prayerFrom(intent) ?: return
        if (prayer == PrayerName.SUNRISE) return
        if (AudioSettingsStore(context).isPrayerMuted(prayer)) return
        context.startForegroundService(AthanService.playIntent(context, prayer))
    }

    companion object {
        private const val FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        fun pendingIntent(context: Context, prayer: PrayerName): PendingIntent {
            val intent = Intent(context, AthanAlarmReceiver::class.java).apply {
                setAction("${ACTION_PREFIX}${prayer.name}")
                putExtra(AthanService.EXTRA_PRAYER, prayer.name)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode(prayer),
                intent,
                FLAG,
            )
        }

        internal fun requestCode(prayer: PrayerName): Int = when (prayer) {
            PrayerName.FAJR -> 501
            PrayerName.DHUHR -> 502
            PrayerName.ASR -> 503
            PrayerName.MAGHRIB -> 504
            PrayerName.ISHA -> 505
            PrayerName.SUNRISE -> 0
        }

        private fun prayerFrom(intent: Intent): PrayerName? {
            val raw = intent.getStringExtra(AthanService.EXTRA_PRAYER) ?: return null
            return runCatching { PrayerName.valueOf(raw) }.getOrNull()
        }

        private const val ACTION_PREFIX = "com.mutazyounes.prayerathan.audio.ALARM_"
    }
}
