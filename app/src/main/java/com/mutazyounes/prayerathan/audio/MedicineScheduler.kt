package com.mutazyounes.prayerathan.audio

import android.app.AlarmManager
import android.content.Context
import com.mutazyounes.prayerathan.PrayerAthanApp
import java.time.Instant
import java.time.ZoneId

class MedicineScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val settings = MedicineSettingsStore(context)

    fun schedule(now: Instant, zone: ZoneId) {
        cancelAll()
        if (!settings.enabled()) return
        val clock = (context.applicationContext as PrayerAthanApp).wallClock
        val alarms = remainingMedicineAlarms(settings.slots(), now, zone)
        alarms.take(MAX_ARMED).forEachIndexed { index, alarm ->
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                clock.alarmEpochMilli(alarm.at),
                MedicineAlarmReceiver.pendingIntent(context, index, alarm.slotId),
            )
        }
    }

    fun cancelAll() {
        for (index in 0 until MAX_ARMED) {
            alarmManager.cancel(MedicineAlarmReceiver.pendingIntent(context, index, "x"))
        }
    }

    companion object {
        const val MAX_ARMED = 32
    }
}
