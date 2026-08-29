package com.mutazyounes.prayerathan.audio

import android.content.Context
import com.mutazyounes.prayerathan.R
import com.mutazyounes.prayerathan.engine.PrayerName

data class AthanSoundChoice(
    val id: String,
    val title: String,
    val subtitle: String,
    val rawRes: Int,
)

object AthanCatalog {
    const val DEFAULT_ID = "saudi_athan"

    val all: List<AthanSoundChoice> = listOf(
        AthanSoundChoice(
            id = DEFAULT_ID,
            title = "Saudi athan",
            subtitle = "All five prayers · 3:05",
            rawRes = R.raw.athan_saudi,
        ),
    )

    fun choice(id: String): AthanSoundChoice =
        all.firstOrNull { it.id == id } ?: all.first()

    fun byId(id: String): AthanSoundChoice? =
        all.firstOrNull { it.id == id }

    fun rawRes(prayer: PrayerName): Int {
        if (prayer == PrayerName.SUNRISE) error("no athan at sunrise")
        return all.first().rawRes
    }
}

class AudioSettingsStore(
    context: Context,
) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun soundId(): String =
        prefs.getString(KEY_SOUND, AthanCatalog.DEFAULT_ID) ?: AthanCatalog.DEFAULT_ID

    fun setSoundId(id: String) {
        prefs.edit().putString(KEY_SOUND, AthanCatalog.choice(id).id).apply()
    }

    fun athkarEnabled(): Boolean = prefs.getBoolean(KEY_ATHKAR, false)

    fun setAthkarEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ATHKAR, enabled).apply()
    }

    fun isPrayerMuted(prayer: PrayerName): Boolean =
        prayer in mutedPrayers()

    fun mutedPrayers(): Set<PrayerName> {
        val raw = prefs.getStringSet(KEY_MUTED_PRAYERS, DEFAULT_MUTED) ?: DEFAULT_MUTED
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
        private const val KEY_SOUND = "athan_sound"
        private const val KEY_ATHKAR = "athkar_enabled"
        private const val KEY_MUTED_PRAYERS = "muted_prayers"
        private val DEFAULT_MUTED: Set<String> =
            PrayerName.athanTargets().map { it.name }.toSet()
    }
}
