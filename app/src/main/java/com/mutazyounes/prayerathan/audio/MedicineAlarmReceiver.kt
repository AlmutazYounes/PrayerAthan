package com.mutazyounes.prayerathan.audio

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mutazyounes.prayerathan.PrayerAthanApp
import java.time.ZoneId

class MedicineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as PrayerAthanApp
        val settings = MedicineSettingsStore(app)
        val now = app.wallClock.now()
        val location = app.prayerEngine.location()
        val day = app.prayerEngine.day(now, location)
        val zone = ZoneId.of(location.timeZoneId)
        val slot = medicineSlotFor(settings.slots(), now, zone)
        val key = slot?.let { medicineFireKey(it, now.atZone(zone).toLocalDate()) }
        if (!settings.enabled() ||
            settings.slots().isEmpty() ||
            slot == null ||
            key == null ||
            settings.alreadyFired(key) ||
            app.athanController.playback.value != null ||
            isAthanMinute(athanInstants(day), now, zone)
        ) {
            app.athanController.schedule(day, now)
            return
        }
        // Stop athkar first so a same-minute hourly clip cannot win the race.
        app.athanController.stopAthkar()
        context.startForegroundService(MedicineService.playIntent(context))
    }

    companion object {
        private const val FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        private const val ACTION = "com.mutazyounes.prayerathan.audio.MEDICINE_ALARM"
        const val EXTRA_SLOT = "slot"
        const val EXTRA_INDEX = "index"

        fun pendingIntent(context: Context, index: Int, slotId: String): PendingIntent {
            val intent = Intent(context, MedicineAlarmReceiver::class.java).apply {
                action = "$ACTION.$index"
                putExtra(EXTRA_INDEX, index)
                putExtra(EXTRA_SLOT, slotId)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode(index),
                intent,
                FLAG,
            )
        }

        internal fun requestCode(index: Int): Int = 700 + index.coerceIn(0, MedicineScheduler.MAX_ARMED - 1)
    }
}
