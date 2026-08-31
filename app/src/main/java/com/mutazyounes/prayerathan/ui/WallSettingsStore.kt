package com.mutazyounes.prayerathan.ui

import android.content.Context

class WallSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun nightBlackout(): Boolean {
        return prefs.getBoolean(KEY_NIGHT_BLACKOUT, true)
    }

    fun setNightBlackout(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NIGHT_BLACKOUT, enabled).apply()
    }

    companion object {
        private const val PREFS = "prayerathan_wall"
        private const val KEY_NIGHT_BLACKOUT = "night_blackout"
    }
}
