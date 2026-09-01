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
    /** 1 = full ring at start of interval; shrinks toward 0 as next prayer nears. */
    val nextPrayerRing: Float,
    val athanPlaying: Boolean,
    val playingName: PrayerName?,
    val athkarPlaying: Boolean,
    val athkarCaption: String,
    val cells: List<PrayerCellState>,
    val twelveHour: Boolean,
    val weatherLine: String,
    val weatherCondition: String,
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
            nextPrayerRing = 1f,
            athanPlaying = false,
            playingName = null,
            athkarPlaying = false,
            athkarCaption = "",
            cells = emptyList(),
            twelveHour = true,
            weatherLine = "",
            weatherCondition = "",
            athanSoundId = "",
            athkarEnabled = false,
            mutedPrayers = PrayerName.athanTargets().toSet(),
            demoId = null,
            nightBlackoutEnabled = true,
            isNightBlackout = false,
        )
    }
}

data class PrayerCellState(
    val name: PrayerName,
    val english: String,
    val time: String,
    val kind: CellKind,
    val muted: Boolean = false,
    val weatherCondition: String = "",
    val weatherTempC: Int? = null,
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
