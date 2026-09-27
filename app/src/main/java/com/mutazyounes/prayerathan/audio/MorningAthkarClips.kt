package com.mutazyounes.prayerathan.audio

import com.mutazyounes.prayerathan.R

/**
 * Fixed morning sequence (adhkar as-sabah). Index 0 fires at 8:05, then every 5 minutes.
 * One clip per alarm slot. Not Quran. Not salawat (hourly uses salawat).
 */
enum class MorningAthkarClip(
    val rawRes: Int,
    val caption: String,
) {
    ASBAHNA_MULK(R.raw.athkar_morning_01, "أصبحنا وأصبح الملك لله"),
    BIKA_ASBAHNA(R.raw.athkar_morning_02, "اللهم بك أصبحنا"),
    SAYYIDUL_ISTIGHFAR(R.raw.athkar_morning_03, "اللهم أنت ربي لا إله إلا أنت"),
    MA_ASBAHA(R.raw.athkar_morning_04, "اللهم ما أصبح بي من نعمة"),
    BISMILLAH_LA_YADURRU(R.raw.athkar_morning_05, "بسم الله الذي لا يضر مع اسمه شيء"),
    YA_HAYYU(R.raw.athkar_morning_06, "يا حي يا قيوم برحمتك أستغيث"),
    ;

    companion object {
        fun at(index: Int): MorningAthkarClip =
            entries[index.coerceIn(0, entries.lastIndex)]

        val count: Int get() = entries.size
    }
}
