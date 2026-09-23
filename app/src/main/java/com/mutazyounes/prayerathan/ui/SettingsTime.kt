package com.mutazyounes.prayerathan.ui

import java.util.Locale

internal object SettingsTime {
    fun offsetLabel(minutes: Int): String {
        if (minutes == 0) return "ISNA"
        if (minutes > 0) return "+$minutes min"
        return "$minutes min"
    }

    fun clock12(hour: Int, minute: Int): String {
        val amPm = if (hour < 12) "AM" else "PM"
        return String.format(Locale.US, "%d:%02d %s", hour12(hour), minute, amPm)
    }

    fun hour12(hour24: Int): Int = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }

    fun isAm(hour24: Int): Boolean = hour24 < 12

    fun hour24(hour12: Int, isAm: Boolean): Int = when {
        isAm && hour12 == 12 -> 0
        !isAm && hour12 == 12 -> 12
        isAm -> hour12
        else -> hour12 + 12
    }

    fun wrapHour12(value: Int): Int = ((value - 1 + 12) % 12) + 1

    fun wrapMinute(value: Int): Int = (value + 60) % 60
}
