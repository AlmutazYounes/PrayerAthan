package com.mutazyounes.prayerathan.audio

import android.app.AlarmManager
import android.content.Context
import com.mutazyounes.prayerathan.PrayerAthanApp
import com.mutazyounes.prayerathan.engine.PrayerDay
import java.time.Instant
import java.time.ZoneId

class AthkarScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(day: PrayerDay, now: Instant, zone: ZoneId) {
        cancelAll()
        val clock = (context.applicationContext as PrayerAthanApp).wallClock
        val medicine = MedicineSettingsStore(context)
        val slots = if (medicine.enabled()) medicine.slots() else emptyList()
        for (at in remainingAthkarAlarms(day, now, zone, slots)) {
            val hour = at.atZone(zone).hour
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                clock.alarmEpochMilli(at),
                AthkarAlarmReceiver.pendingIntent(context, hour),
            )
        }
    }

    fun cancelAll() {
        for (hour in 0..23) {
            alarmManager.cancel(AthkarAlarmReceiver.pendingIntent(context, hour))
        }
    }
}
