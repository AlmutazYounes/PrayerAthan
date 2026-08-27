package com.mutazyounes.prayerathan.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PrayerCalculatorTest {

    private lateinit var engine: PrayerCalculator
    private val albanyZone: ZoneId = ZoneId.of("America/New_York")
    private val knownDate: LocalDate = LocalDate.of(2026, 8, 27)

    @Before
    fun setUp() {
        engine = PrayerCalculator(InMemoryLocationStore())
    }

    @Test
    fun albanyKnownDateHasSixTimes() {
        val noon = knownDate.atTime(12, 0).atZone(albanyZone).toInstant()
        val day = engine.day(noon)

        assertEquals(SavedLocation.albany, engine.location())
        assertEquals(knownDate, day.localDate)
        assertEquals(6, day.times.size)
        assertEquals(
            listOf(
                PrayerName.FAJR,
                PrayerName.SUNRISE,
                PrayerName.DHUHR,
                PrayerName.ASR,
                PrayerName.MAGHRIB,
                PrayerName.ISHA,
            ),
            day.times.map { it.name },
        )

        val instants = day.times.map { it.at }
        assertEquals(instants.sorted(), instants)
        assertEquals(6, instants.toSet().size)
        day.times.forEach { prayer ->
            assertEquals(knownDate, prayer.at.atZone(albanyZone).toLocalDate())
        }
        assertNotEquals(PrayerName.SUNRISE, day.nextAthan)
        assertTrue(day.nextAthan in PrayerName.athanTargets())
    }

    @Test
    fun afterIshaNextIsTomorrowFajr() {
        val noon = knownDate.atTime(12, 0).atZone(albanyZone).toInstant()
        val today = engine.day(noon)
        val isha = today.times.first { it.name == PrayerName.ISHA }.at
        val afterIsha = engine.day(isha.plusSeconds(60))

        assertEquals(knownDate, afterIsha.localDate)
        assertEquals(PrayerName.FAJR, afterIsha.nextAthan)

        val tomorrowNoon = knownDate.plusDays(1).atTime(12, 0).atZone(albanyZone).toInstant()
        val tomorrowFajr = engine.day(tomorrowNoon).times.first { it.name == PrayerName.FAJR }.at
        assertEquals(tomorrowFajr, afterIsha.nextAthanAt)
        assertTrue(afterIsha.nextAthanAt.isAfter(isha))
    }

    @Test
    fun afterMidnightBeforeFajrIsTodaysFajr() {
        val nextMorning = knownDate.plusDays(1)
        val halfPastMidnight = nextMorning.atTime(0, 30).atZone(albanyZone).toInstant()
        val day = engine.day(halfPastMidnight)
        val fajr = day.times.first { it.name == PrayerName.FAJR }

        assertEquals(nextMorning, day.localDate)
        assertEquals(PrayerName.FAJR, day.nextAthan)
        assertEquals(fajr.at, day.nextAthanAt)
        assertTrue(fajr.at.isAfter(halfPastMidnight))
    }

    @Test
    fun sunriseIsNeverNextAthan() {
        var cursor = knownDate.atStartOfDay(albanyZone).toInstant()
        val end = knownDate.plusDays(1).atStartOfDay(albanyZone).toInstant()
        while (cursor.isBefore(end)) {
            assertNotEquals(PrayerName.SUNRISE, engine.day(cursor).nextAthan)
            cursor = cursor.plusSeconds(600)
        }
    }

    @Test
    fun afterFajrNextIsDhuhr() {
        val noon = knownDate.atTime(12, 0).atZone(albanyZone).toInstant()
        val today = engine.day(noon)
        val fajr = today.times.first { it.name == PrayerName.FAJR }.at
        val sunrise = today.times.first { it.name == PrayerName.SUNRISE }.at

        val afterFajr = engine.day(fajr.plusSeconds(1))
        assertEquals(PrayerName.DHUHR, afterFajr.nextAthan)
        assertTrue(sunrise.isAfter(fajr))
        assertTrue(afterFajr.nextAthanAt.isAfter(sunrise))
    }

    @Test
    fun jordanZoneIsAsiaAmman() {
        val now = Instant.parse("2026-08-27T16:00:00Z")
        val clocks = engine.clocks(now)

        assertEquals(TimeZones.ASIA_AMMAN, clocks.jordan.zone.id)
        assertEquals(TimeZones.asiaAmman, clocks.jordan.zone)
        assertEquals(now, clocks.jordan.toInstant())
        assertEquals(now, clocks.albany.toInstant())
        assertEquals("America/New_York", clocks.albany.zone.id)
    }

    @Test
    fun jordanIsNotAlbanyPlusFixedHours() {
        val summer = engine.clocks(Instant.parse("2026-08-27T16:00:00Z"))
        val winter = engine.clocks(Instant.parse("2026-01-15T16:00:00Z"))

        assertEquals(7, hoursJordanAheadOfAlbany(summer))
        assertEquals(8, hoursJordanAheadOfAlbany(winter))
        assertNotEquals(
            hoursJordanAheadOfAlbany(summer),
            hoursJordanAheadOfAlbany(winter),
        )
    }

    @Test
    fun remainingToNextIsNeverNegative() {
        val noon = knownDate.atTime(12, 0).atZone(albanyZone).toInstant()
        val day = engine.day(noon)
        val samples = listOf(
            noon,
            day.times.first { it.name == PrayerName.FAJR }.at,
            day.times.first { it.name == PrayerName.FAJR }.at.plusSeconds(1),
            day.times.first { it.name == PrayerName.ISHA }.at.plusSeconds(1),
        )
        samples.forEach { now ->
            val remaining = engine.remainingToNext(now)
            assertFalse(remaining.isNegative)
            val expected = Duration.between(now, engine.day(now).nextAthanAt)
            val clamped = if (expected.isNegative) Duration.ZERO else expected
            assertEquals(clamped, remaining)
        }
    }

    @Test
    fun locationStoreStartsAtAlbanyAndRoundTrips() {
        val store = InMemoryLocationStore()
        assertEquals(SavedLocation.albany, store.read())
        assertEquals(42.6526, store.read().latitude, 0.0)
        assertEquals(-73.7562, store.read().longitude, 0.0)
        assertEquals("America/New_York", store.read().timeZoneId)
        assertEquals("Albany, NY", store.read().label)

        val amman = SavedLocation(
            label = "Amman",
            latitude = 31.9454,
            longitude = 35.9284,
            timeZoneId = TimeZones.ASIA_AMMAN,
        )
        store.write(amman)
        assertEquals(amman, store.read())
    }

    private fun hoursJordanAheadOfAlbany(clocks: WallClocks): Long {
        val deltaSeconds = clocks.jordan.offset.totalSeconds - clocks.albany.offset.totalSeconds
        return deltaSeconds / 3600L
    }
}
