package com.mutazyounes.prayerathan.engine

import android.content.Context

interface OffsetStore {
    fun read(): PrayerOffsets
    fun write(offsets: PrayerOffsets)
}

class InMemoryOffsetStore(
    initial: PrayerOffsets = PrayerOffsets.ZERO,
) : OffsetStore {
    private var stored: PrayerOffsets = initial

    override fun read(): PrayerOffsets = stored

    override fun write(offsets: PrayerOffsets) {
        stored = offsets
    }
}

class PrefsOffsetStore internal constructor(
    private val prefs: OffsetPrefsBackend,
) : OffsetStore {

    constructor(context: Context) : this(
        SharedPreferencesOffsetBackend(context.applicationContext),
    )

    override fun read(): PrayerOffsets {
        val minutes = PrayerOffsets.ALL.associateWith { name ->
            PrayerOffsets.clamp(prefs.getInt(key(name), 0))
        }
        return PrayerOffsets(minutes)
    }

    override fun write(offsets: PrayerOffsets) {
        prefs.write(
            PrayerOffsets.ALL.associate { name ->
                key(name) to offsets.minutesOf(name)
            },
        )
    }

    companion object {
        internal const val PREFS = "prayerathan_offsets"

        internal fun key(name: PrayerName): String = "offset_${name.name}"
    }
}

internal interface OffsetPrefsBackend {
    fun getInt(key: String, default: Int): Int
    fun write(values: Map<String, Int>)
}

private class SharedPreferencesOffsetBackend(
    context: Context,
) : OffsetPrefsBackend {
    private val prefs = context.getSharedPreferences(
        PrefsOffsetStore.PREFS,
        Context.MODE_PRIVATE,
    )

    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)

    override fun write(values: Map<String, Int>) {
        prefs.edit().apply {
            values.forEach { (key, value) -> putInt(key, value) }
            apply()
        }
    }
}
