package com.mutazyounes.prayerathan.ui

import android.content.Context

class WallSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun themeMode(): ThemeMode {
        val raw = prefs.getString(KEY_THEME, ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name
        return runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.LIGHT)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME, mode.name).apply()
    }

    companion object {
        private const val PREFS = "prayerathan_wall"
        private const val KEY_THEME = "theme_mode"
    }
}
