package com.mutazyounes.prayerathan.shell

import android.content.Context

class NtpTimeStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun correctionMs(): Long = prefs.getLong(KEY_CORRECTION, 0L)

    fun lastSyncAtMs(): Long = prefs.getLong(KEY_SYNCED_AT, 0L)

    fun isDue(nowSystemMs: Long = System.currentTimeMillis()): Boolean {
        val last = lastSyncAtMs()
        if (last == 0L) return true
        return nowSystemMs - last >= SYNC_EVERY_MS
    }

    fun save(correctionMs: Long, nowSystemMs: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putLong(KEY_CORRECTION, correctionMs)
            .putLong(KEY_SYNCED_AT, nowSystemMs)
            .apply()
    }

    companion object {
        const val PREFS = "prayerathan_wall_clock"
        const val KEY_CORRECTION = "ntp_correction_ms"
        const val KEY_SYNCED_AT = "ntp_synced_at_ms"
        const val SYNC_EVERY_MS = 24L * 60L * 60L * 1000L
    }
}
