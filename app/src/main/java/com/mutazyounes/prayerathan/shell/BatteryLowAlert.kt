package com.mutazyounes.prayerathan.shell

import android.media.AudioManager
import android.media.ToneGenerator

/**
 * Short alert when portrait battery crosses below 20%.
 * Not athan. Not a foreground service.
 */
object BatteryLowAlert {
    const val LOW_THRESHOLD = 20

    private var wasLow: Boolean? = null

    /**
     * Play once when percent enters the low band.
     * Resets so a later drop after recovery can alert again.
     */
    fun onPercent(percent: Int) {
        if (percent < 0) return
        val low = percent < LOW_THRESHOLD
        val previous = wasLow
        wasLow = low
        if (previous == false && low) {
            playBeep()
        }
        if (previous == null && low) {
            // Cold start already low: alert once.
            playBeep()
        }
    }

    private fun playBeep() {
        try {
            val tone = ToneGenerator(AudioManager.STREAM_ALARM, 80)
            tone.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 450)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                tone.release()
            }, 600)
        } catch (_: Exception) {
            // Ignore missing audio path on emulator.
        }
    }
}
