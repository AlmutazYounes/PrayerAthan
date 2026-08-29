package com.mutazyounes.prayerathan.ui

import com.mutazyounes.prayerathan.engine.PrayerName

data class WallUiState(
    val locationLabel: String,
    val locationCity: String,
    val locationLatitude: String,
    val locationLongitude: String,
    val locationTimeZoneId: String,
    val locationError: String?,
    val gregorianDate: String,
    val weekday: String,
    val albanyTime: String,
    val albanyAmPm: String,
    val jordanTime: String,
    val jordanAmPm: String,
    val nextLabel: String,
    val countdown: String,
    val athanPlaying: Boolean,
    val playingName: PrayerName?,
    val athkarPlaying: Boolean,
    val athkarCaption: String,
    val cells: List<PrayerCellState>,
    val twelveHour: Boolean,
    val themeMode: ThemeMode,
    val darkTheme: Boolean,
    val weatherLine: String,
    val athanSoundId: String,
    val athkarEnabled: Boolean,
    val mutedPrayers: Set<PrayerName>,
    val demoId: String?,
    val nightBlackoutEnabled: Boolean,
    val isNightBlackout: Boolean,
) {
    companion object {
        val Empty = WallUiState(
            locationLabel = "",
            locationCity = "",
            locationLatitude = "",
            locationLongitude = "",
            locationTimeZoneId = "",
            locationError = null,
            gregorianDate = "",
            weekday = "",
            albanyTime = "",
            albanyAmPm = "",
            jordanTime = "",
            jordanAmPm = "",
            nextLabel = "",
            countdown = "00:00:00",
            athanPlaying = false,
            playingName = null,
            athkarPlaying = false,
            athkarCaption = "",
            cells = emptyList(),
            twelveHour = true,
            themeMode = ThemeMode.AUTO,
            darkTheme = true,
            weatherLine = "",
            athanSoundId = "",
            athkarEnabled = false,
            mutedPrayers = PrayerName.athanTargets().toSet(),
            demoId = null,
            nightBlackoutEnabled = true,
            isNightBlackout = false,
        )
    }
}

enum class ThemeMode { LIGHT, DARK, AUTO }

data class PrayerCellState(
    val name: PrayerName,
    val english: String,
    val time: String,
    val kind: CellKind,
    val muted: Boolean = false,
)

enum class CellKind { PAST, NEXT, LATER }

enum class ClockEmphasis { Local, World }

fun PrayerName.englishLabel(): String = when (this) {
    PrayerName.FAJR -> "FAJR"
    PrayerName.SUNRISE -> "SUNRISE"
    PrayerName.DHUHR -> "DHUHR"
    PrayerName.ASR -> "ASR"
    PrayerName.MAGHRIB -> "MAGHRIB"
    PrayerName.ISHA -> "ISHA"
}
