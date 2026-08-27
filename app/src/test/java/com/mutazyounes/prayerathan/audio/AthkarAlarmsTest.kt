package com.mutazyounes.prayerathan.audio

import com.mutazyounes.prayerathan.engine.PrayerDay
import com.mutazyounes.prayerathan.engine.PrayerInstant
import com.mutazyounes.prayerathan.engine.PrayerName
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AthkarAlarmsTest {

    private val zone = ZoneId.of("America/New_York")
    private val fajr = Instant.parse("2026-08-27T09:09:00Z")
    private val sunrise = Instant.parse("2026-08-27T10:40:00Z")
    private val dhuhr = Instant.parse("2026-08-27T17:00:00Z")
    private val asr = Instant.parse("2026-08-27T20:30:00Z")
    private val maghrib = Instant.parse("2026-08-27T23:40:00Z")
    private val isha = Instant.parse("2026-08-28T01:10:00Z")
    private val tomorrowFajr = Instant.parse("2026-08-28T09:10:00Z")
    private val sixAm = Instant.parse("2026-08-27T10:00:00Z")
    private val eightAm = Instant.parse("2026-08-27T12:00:00Z")
    private val onePm = Instant.parse("2026-08-27T17:00:00Z")
    private val ninePm = Instant.parse("2026-08-28T01:00:00Z")
    private val tenPm = Instant.parse("2026-08-28T02:00:00Z")
    private val tomorrowEightAm = Instant.parse("2026-08-28T12:00:00Z")

    private val today = PrayerDay(
        localDate = LocalDate.of(2026, 8, 27),
        times = listOf(
            PrayerInstant(PrayerName.FAJR, fajr),
            PrayerInstant(PrayerName.SUNRISE, sunrise),
            PrayerInstant(PrayerName.DHUHR, dhuhr),
            PrayerInstant(PrayerName.ASR, asr),
            PrayerInstant(PrayerName.MAGHRIB, maghrib),
            PrayerInstant(PrayerName.ISHA, isha),
        ),
        nextAthan = PrayerName.DHUHR,
        nextAthanAt = dhuhr,
    )

    @Test
    fun afterFajrStartsAtEightAndSkipsDhuhrHour() {
        val hours = remainingAthkarAlarms(today, fajr.plusSeconds(1), zone)
        assertEquals(eightAm, hours.first())
        assertFalse(hours.contains(sixAm))
        assertFalse(hours.contains(onePm))
        assertTrue(hours.contains(ninePm))
        assertFalse(hours.contains(tenPm))
        assertFalse(hours.any { it.atZone(zone).hour < 8 || it.atZone(zone).hour >= 22 })
    }

    @Test
    fun afterIshaOnlyTomorrowEight() {
        val afterIsha = today.copy(
            nextAthan = PrayerName.FAJR,
            nextAthanAt = tomorrowFajr,
        )
        val hours = remainingAthkarAlarms(afterIsha, isha.plusSeconds(1), zone)
        assertEquals(listOf(tomorrowEightAm), hours)
    }

    @Test
    fun quietFromTenPmUntilEightAm() {
        val lateIsha = Instant.parse("2026-08-28T03:10:00Z")
        val lateDay = today.copy(
            times = today.times.map {
                if (it.name == PrayerName.ISHA) it.copy(at = lateIsha) else it
            },
        )
        val hours = remainingAthkarAlarms(lateDay, fajr.plusSeconds(1), zone)
        assertFalse(hours.contains(tenPm))
        assertFalse(hours.any { it.atZone(zone).hour >= 22 })
        assertFalse(isAthkarWindow(lateDay, tenPm, zone))
        assertTrue(isAthkarWindow(today, eightAm, zone))
        assertFalse(isAthkarWindow(today, fajr.plusSeconds(1), zone))
    }

    @Test
    fun beforeFajrStaysSilentUntilEight() {
        val beforeFajr = today.copy(
            nextAthan = PrayerName.FAJR,
            nextAthanAt = fajr,
        )
        val hours = remainingAthkarAlarms(beforeFajr, fajr.minusSeconds(60), zone)
        assertEquals(eightAm, hours.first())
        assertFalse(isAthkarWindow(today, fajr.minusSeconds(1), zone))
        assertFalse(isAthkarWindow(today, isha.plusSeconds(1), zone))
    }
}
