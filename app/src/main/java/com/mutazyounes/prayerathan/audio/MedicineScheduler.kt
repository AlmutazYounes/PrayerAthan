package com.mutazyounes.prayerathan.audio

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mutazyounes.prayerathan.MainActivity
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
        if (!settings.enabled() || settings.slots().isEmpty()) return
        val clock = (context.applicationContext as PrayerAthanApp).wallClock
        val alarms = remainingMedicineAlarms(settings.slots(), now, zone)
        alarms.take(MAX_ARMED).forEachIndexed { index, alarm ->
            val show = PendingIntent.getActivity(
                context,
                SHOW_REQUEST_BASE + index,
                Intent(context, MainActivity::class.java),
                FLAG,
            )
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(clock.alarmEpochMilli(alarm.at), show),
                MedicineAlarmReceiver.pendingIntent(context, index, alarm.slotId),
            )
        }
        maybeCatchUp(now, zone)
    }

    fun cancelAll() {
        for (index in 0 until MAX_ARMED) {
            alarmManager.cancel(MedicineAlarmReceiver.pendingIntent(context, index, "x"))
        }
    }

    /** If Mutaz enables a slot that is already due (same minute / grace), play now. */
    private fun maybeCatchUp(now: Instant, zone: ZoneId) {
        val slot = medicineSlotFor(settings.slots(), now, zone) ?: return
        val key = medicineFireKey(slot, now.atZone(zone).toLocalDate())
        if (settings.alreadyFired(key)) return
        val app = context.applicationContext as PrayerAthanApp
        if (app.athanController.playback.value != null) return
        if (app.athanController.medicinePlayback.value != null) return
        context.startForegroundService(MedicineService.playIntent(context))
    }

    companion object {
        const val MAX_ARMED = 32
        private const val SHOW_REQUEST_BASE = 720
        private const val FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    }
}
