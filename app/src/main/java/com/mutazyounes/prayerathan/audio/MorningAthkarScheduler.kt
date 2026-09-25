package com.mutazyounes.prayerathan.audio

import android.app.AlarmManager
import android.content.Context
import com.mutazyounes.prayerathan.PrayerAthanApp
import com.mutazyounes.prayerathan.engine.PrayerDay
import java.time.Instant
import java.time.ZoneId

class MorningAthkarScheduler(
    private val context: Context,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(day: PrayerDay, now: Instant, zone: ZoneId) {
        cancelAll()
        val clock = (context.applicationContext as PrayerAthanApp).wallClock
        val medicine = MedicineSettingsStore(context)
        val slots = if (medicine.enabled()) medicine.slots() else emptyList()
        val athanTimes = athanInstants(day)
        for (slot in remainingMorningAthkarAlarms(
            now = now,
            zone = zone,
            clipCount = MorningAthkarClip.count,
            athanTimes = athanTimes,
            medicineSlots = slots,
        )) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                clock.alarmEpochMilli(slot.at),
                MorningAthkarAlarmReceiver.pendingIntent(context, slot.index),
            )
        }
    }

    fun cancelAll() {
        for (index in 0 until MorningAthkarClip.count) {
            alarmManager.cancel(MorningAthkarAlarmReceiver.pendingIntent(context, index))
        }
    }
}
