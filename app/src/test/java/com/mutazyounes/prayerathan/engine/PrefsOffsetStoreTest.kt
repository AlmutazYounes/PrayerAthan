package com.mutazyounes.prayerathan.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class PrefsOffsetStoreTest {

    @Test
    fun emptyPrefsAreZero() {
        val store = PrefsOffsetStore(MemoryOffsetPrefs())
        assertEquals(0, store.read().minutesOf(PrayerName.FAJR))
        assertEquals(0, store.read().minutesOf(PrayerName.SUNRISE))
    }

    @Test
    fun writeRoundTripsOnFreshStore() {
        val prefs = MemoryOffsetPrefs()
        PrefsOffsetStore(prefs).write(
            PrayerOffsets.ZERO.with(PrayerName.MAGHRIB, 3).with(PrayerName.FAJR, -1),
        )
        val read = PrefsOffsetStore(prefs).read()
        assertEquals(-1, read.minutesOf(PrayerName.FAJR))
        assertEquals(3, read.minutesOf(PrayerName.MAGHRIB))
        assertEquals(0, read.minutesOf(PrayerName.DHUHR))
    }

    @Test
    fun writeClampsOutOfRange() {
        val prefs = MemoryOffsetPrefs()
        PrefsOffsetStore(prefs).write(PrayerOffsets.ZERO.with(PrayerName.ISHA, 90))
        assertEquals(60, PrefsOffsetStore(prefs).read().minutesOf(PrayerName.ISHA))
    }
}

private class MemoryOffsetPrefs : OffsetPrefsBackend {
    private val stored = mutableMapOf<String, Int>()

    override fun getInt(key: String, default: Int): Int = stored[key] ?: default

    override fun write(values: Map<String, Int>) {
        stored.putAll(values)
    }
}
