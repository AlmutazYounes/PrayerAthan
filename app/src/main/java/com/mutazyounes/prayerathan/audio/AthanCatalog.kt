package com.mutazyounes.prayerathan.audio

import android.content.Context
import com.mutazyounes.prayerathan.R
import com.mutazyounes.prayerathan.engine.PrayerName

enum class AthanSlot { FAJR, STANDARD }

data class AthanSoundChoice(
    val id: String,
    val title: String,
    val subtitle: String,
    val slot: AthanSlot,
    val rawRes: Int,
)

object AthanCatalog {
    const val DEFAULT_FAJR_ID = "fajr_haram_2009"
    const val DEFAULT_STANDARD_ID = "std_mala_1439"

    val fajr: List<AthanSoundChoice> = listOf(
        AthanSoundChoice(
            id = DEFAULT_FAJR_ID,
            title = "Haram Fajr",
            subtitle = "13 Nov 2009 · with Fajr addition",
            slot = AthanSlot.FAJR,
            rawRes = R.raw.athan_fajr,
        ),
    )

    val standard: List<AthanSoundChoice> = listOf(
        AthanSoundChoice(
            id = DEFAULT_STANDARD_ID,
            title = "Ali Mala",
            subtitle = "Haram Isha, Muharram 1439",
            slot = AthanSlot.STANDARD,
            rawRes = R.raw.athan_standard,
        ),
    )

    fun fajr(id: String): AthanSoundChoice =
        fajr.firstOrNull { it.id == id } ?: fajr.first()

    fun standard(id: String): AthanSoundChoice =
        standard.firstOrNull { it.id == id } ?: standard.first()

    fun byId(id: String): AthanSoundChoice? =
        fajr.firstOrNull { it.id == id } ?: standard.firstOrNull { it.id == id }

    fun rawRes(prayer: PrayerName, fajrId: String, standardId: String): Int {
        return when (prayer) {
            PrayerName.FAJR -> fajr(fajrId).rawRes
            PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA ->
                standard(standardId).rawRes
            PrayerName.SUNRISE -> error("no athan at sunrise")
        }
    }
}

class AudioSettingsStore(
    context: Context,
) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun fajrSoundId(): String =
        prefs.getString(KEY_FAJR, AthanCatalog.DEFAULT_FAJR_ID) ?: AthanCatalog.DEFAULT_FAJR_ID

    fun setFajrSoundId(id: String) {
        prefs.edit().putString(KEY_FAJR, AthanCatalog.fajr(id).id).apply()
    }

    fun standardSoundId(): String =
        prefs.getString(KEY_STANDARD, AthanCatalog.DEFAULT_STANDARD_ID)
            ?: AthanCatalog.DEFAULT_STANDARD_ID

    fun setStandardSoundId(id: String) {
        prefs.edit().putString(KEY_STANDARD, AthanCatalog.standard(id).id).apply()
    }

    fun athkarEnabled(): Boolean = prefs.getBoolean(KEY_ATHKAR, true)

    fun setAthkarEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ATHKAR, enabled).apply()
    }

    fun isPrayerMuted(prayer: PrayerName): Boolean =
        prayer in mutedPrayers()

    fun mutedPrayers(): Set<PrayerName> {
        val raw = prefs.getStringSet(KEY_MUTED_PRAYERS, emptySet()) ?: emptySet()
        return raw.mapNotNull { name ->
            runCatching { PrayerName.valueOf(name) }.getOrNull()
        }.toSet()
    }

    fun setPrayerMuted(prayer: PrayerName, muted: Boolean) {
        val current = mutedPrayers().toMutableSet()
        if (muted) {
            current.add(prayer)
        } else {
            current.remove(prayer)
        }
        prefs.edit().putStringSet(KEY_MUTED_PRAYERS, current.map { it.name }.toSet()).apply()
    }

    companion object {
        private const val PREFS = "prayerathan_audio"
        private const val KEY_FAJR = "fajr_sound"
        private const val KEY_STANDARD = "standard_sound"
        private const val KEY_ATHKAR = "athkar_enabled"
        private const val KEY_MUTED_PRAYERS = "muted_prayers"
    }
}
