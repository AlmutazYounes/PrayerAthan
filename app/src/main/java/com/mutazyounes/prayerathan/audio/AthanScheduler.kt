package com.mutazyounes.prayerathan.audio

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mutazyounes.prayerathan.MainActivity
import com.mutazyounes.prayerathan.PrayerAthanApp
import com.mutazyounes.prayerathan.engine.PrayerDay
import com.mutazyounes.prayerathan.engine.PrayerInstant
import com.mutazyounes.prayerathan.engine.PrayerName
import java.time.Instant

class AthanScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(day: PrayerDay, now: Instant) {
        cancelAll()
        val muted = AudioSettingsStore(context).mutedPrayers()
        for (instant in remainingAthanAlarms(day, now, muted)) {
            setClock(instant)
        }
    }

    private fun setClock(instant: PrayerInstant) {
        val clock = (context.applicationContext as PrayerAthanApp).wallClock
        val triggerAt = clock.alarmEpochMilli(instant.at)
        val show = PendingIntent.getActivity(
            context,
            SHOW_REQUEST,
            Intent(context, MainActivity::class.java),
            FLAG,
        )
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, show),
            AthanAlarmReceiver.pendingIntent(context, instant.name),
        )
    }

    private fun cancelAll() {
        for (name in PrayerName.athanTargets()) {
            alarmManager.cancel(AthanAlarmReceiver.pendingIntent(context, name))
        }
    }

    companion object {
        private const val SHOW_REQUEST = 500
        private const val FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
