package com.mutazyounes.prayerathan.audio

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mutazyounes.prayerathan.PrayerAthanApp
import com.mutazyounes.prayerathan.engine.PrayerDay
import java.time.Instant
import java.time.ZoneId

class MorningAthkarAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as PrayerAthanApp
        val index = intent.getIntExtra(EXTRA_INDEX, -1)
        if (!AudioSettingsStore(app).morningAthkarEnabled()) {
            reschedule(app)
            return
        }
        if (index !in 0 until MorningAthkarClip.count) {
            reschedule(app)
            return
        }
        val now = app.wallClock.now()
        val location = app.prayerEngine.location()
        val day = app.prayerEngine.day(now, location)
        val zone = ZoneId.of(location.timeZoneId)
        val medicineSettings = MedicineSettingsStore(app)
        val medicineDue = athkarYieldsToMedicine(
            medicineSettings.enabled(),
            medicineSettings.slots(),
            now,
            zone,
        )
        val athanPlaying = app.athanController.playback.value != null
        val athanMinute = isAthanMinute(athanInstants(day), now, zone)
        if (shouldStartMedicineFromAthkar(medicineDue, athanPlaying, athanMinute)) {
            context.startForegroundService(MedicineService.playIntent(context))
            reschedule(app, day, now)
            return
        }
        if (!shouldPlayMorningAthkar(
                morningEnabled = true,
                athanPlaying = athanPlaying,
                medicinePlaying = app.athanController.medicinePlayback.value != null,
                athanMinute = athanMinute,
                medicineDue = medicineDue,
            )
        ) {
            reschedule(app, day, now)
            return
        }
        context.startForegroundService(AthkarService.morningPlayIntent(context, index))
    }

    private fun reschedule(
        app: PrayerAthanApp,
        day: PrayerDay? = null,
        now: Instant? = null,
    ) {
        val at = now ?: app.wallClock.now()
        val prayerDay = day ?: app.prayerEngine.day(at)
        app.athanController.schedule(prayerDay, at)
    }

    companion object {
        private const val FLAG = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        private const val ACTION = "com.mutazyounes.prayerathan.audio.MORNING_ATHKAR_ALARM"
        const val EXTRA_INDEX = "index"

        fun pendingIntent(context: Context, index: Int): PendingIntent {
            val intent = Intent(context, MorningAthkarAlarmReceiver::class.java).apply {
                action = "$ACTION.$index"
                putExtra(EXTRA_INDEX, index)
            }
            return PendingIntent.getBroadcast(
                context,
                requestCode(index),
                intent,
                FLAG,
            )
        }

        internal fun requestCode(index: Int): Int =
            700 + index.coerceIn(0, MorningAthkarClip.count)
    }
}
