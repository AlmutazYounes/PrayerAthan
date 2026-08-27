package com.mutazyounes.prayerathan.engine

enum class PrayerName {
    FAJR,
    SUNRISE,
    DHUHR,
    ASR,
    MAGHRIB,
    ISHA,
    ;

    companion object {
        fun athanTargets(): List<PrayerName> = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
    }
}
