package com.mutazyounes.prayerathan.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PrefsLocationStoreTest {

    private val amman = SavedLocation(
        label = "Amman",
        latitude = 31.9454,
        longitude = 35.9284,
        timeZoneId = TimeZones.ASIA_AMMAN,
    )

    @Test
    fun emptyPrefsReturnAlbany() {
        val store = PrefsLocationStore(MemoryLocationPrefs())
        assertEquals(SavedLocation.albany, store.read())
        assertEquals("Albany, NY", store.read().label)
        assertEquals(42.6526, store.read().latitude, 0.0)
        assertEquals(-73.7562, store.read().longitude, 0.0)
        assertEquals("America/New_York", store.read().timeZoneId)
    }

    @Test
    fun writeRoundTripsOnFreshStore() {
        val prefs = MemoryLocationPrefs()
        PrefsLocationStore(prefs).write(amman)

        val fresh = PrefsLocationStore(prefs)
        assertEquals(amman, fresh.read())
        assertNotEquals(SavedLocation.albany, fresh.read())
    }

    @Test
    fun invalidZoneFallsBackToAlbany() {
        val prefs = MemoryLocationPrefs()
        PrefsLocationStore(prefs).write(
            SavedLocation(
                label = "Nowhere",
                latitude = 0.0,
                longitude = 0.0,
                timeZoneId = "Not/ARealZone",
            ),
        )
        assertEquals(SavedLocation.albany, PrefsLocationStore(prefs).read())
    }

    @Test
    fun unreadableCoordsFallBackToAlbany() {
        val prefs = MemoryLocationPrefs()
        prefs.write(
            mapOf(
                PrefsLocationStore.KEY_LABEL to "Broken",
                PrefsLocationStore.KEY_LATITUDE to "not-a-number",
                PrefsLocationStore.KEY_LONGITUDE to "35.9",
                PrefsLocationStore.KEY_TIMEZONE to TimeZones.ASIA_AMMAN,
            ),
        )
        assertEquals(SavedLocation.albany, PrefsLocationStore(prefs).read())
    }

    @Test
    fun calculatorUsesWrittenCoords() {
        val prefs = MemoryLocationPrefs()
        val store = PrefsLocationStore(prefs)
        val engine = PrayerCalculator(store)
        store.write(amman)

        assertEquals(amman, engine.location())
        val now = Instant.parse("2026-08-27T12:00:00Z")
        val day = engine.day(now)
        val ammanDate = now.atZone(ZoneId.of(TimeZones.ASIA_AMMAN)).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 27), ammanDate)
        assertEquals(ammanDate, day.localDate)
        val albanyDay = PrayerCalculator(InMemoryLocationStore()).day(now)
        assertNotEquals(albanyDay.times.map { it.at }, day.times.map { it.at })
    }
}

class SavedLocationParseTest {

    @Test
    fun parseAcceptsAmman() {
        val parsed = SavedLocation.parse("  Amman  ", 31.9454, 35.9284, "Asia/Amman")
        assertEquals(
            SavedLocation("Amman", 31.9454, 35.9284, TimeZones.ASIA_AMMAN),
            parsed,
        )
    }

    @Test
    fun parseRejectsUnknownZone() {
        assertNull(SavedLocation.parse("X", 0.0, 0.0, "Not/ARealZone"))
        assertNull(SavedLocation.parse("X", 0.0, 0.0, ""))
    }

    @Test
    fun parseRejectsOutOfRangeCoords() {
        assertNull(SavedLocation.parse("X", 90.1, 0.0, TimeZones.AMERICA_NEW_YORK))
        assertNull(SavedLocation.parse("X", -90.1, 0.0, TimeZones.AMERICA_NEW_YORK))
        assertNull(SavedLocation.parse("X", 0.0, 180.1, TimeZones.AMERICA_NEW_YORK))
        assertNull(SavedLocation.parse("X", 0.0, -180.1, TimeZones.AMERICA_NEW_YORK))
        assertNull(SavedLocation.parse("X", Double.NaN, 0.0, TimeZones.AMERICA_NEW_YORK))
    }

    @Test
    fun parseAcceptsPolesAndDateLine() {
        assertEquals(
            SavedLocation("North", 90.0, 180.0, TimeZones.AMERICA_NEW_YORK),
            SavedLocation.parse("North", 90.0, 180.0, TimeZones.AMERICA_NEW_YORK),
        )
        assertEquals(
            SavedLocation("South", -90.0, -180.0, TimeZones.AMERICA_NEW_YORK),
            SavedLocation.parse("South", -90.0, -180.0, TimeZones.AMERICA_NEW_YORK),
        )
    }
}

internal class MemoryLocationPrefs : LocationPrefsBackend {
    private val data = mutableMapOf<String, String>()

    override fun getString(key: String): String? = data[key]

    override fun contains(key: String): Boolean = key in data

    override fun write(values: Map<String, String>) {
        data.putAll(values)
    }
}
