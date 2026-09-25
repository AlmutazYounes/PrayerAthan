package com.mutazyounes.prayerathan.audio

import com.mutazyounes.prayerathan.R

/**
 * Fixed morning sequence. Index 0 fires at 8:05, then every 5 minutes.
 * One clip per alarm slot. Not Quran.
 */
enum class MorningAthkarClip(
    val rawRes: Int,
    val caption: String,
) {
    SALAWAT(R.raw.athkar_salawat, "اللهم صل على محمد"),
    EQUALS(R.raw.athkar_morning_01, "اللهم صل على محمد"),
    SEE_PROPHET(R.raw.athkar_morning_02, "اللهم صل على محمد"),
    TAHIYYAT(R.raw.athkar_morning_03, "اللهم صل على محمد"),
    SHAFII(R.raw.athkar_morning_04, "اللهم صل على محمد"),
    SAADAH(R.raw.athkar_morning_05, "اللهم صل على محمد"),
    ;

    companion object {
        fun at(index: Int): MorningAthkarClip =
            entries[index.coerceIn(0, entries.lastIndex)]

        val count: Int get() = entries.size
    }
}
