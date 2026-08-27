package com.mutazyounes.prayerathan.audio

import com.mutazyounes.prayerathan.engine.PrayerDay
import com.mutazyounes.prayerathan.engine.PrayerInstant
import com.mutazyounes.prayerathan.engine.PrayerName
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AthanAlarmsTest {

    private val fajr = Instant.parse("2026-08-27T09:09:00Z")
    private val sunrise = Instant.parse("2026-08-27T10:40:00Z")
    private val dhuhr = Instant.parse("2026-08-27T17:00:00Z")
    private val asr = Instant.parse("2026-08-27T20:30:00Z")
    private val maghrib = Instant.parse("2026-08-27T23:40:00Z")
    private val isha = Instant.parse("2026-08-28T01:10:00Z")
    private val tomorrowFajr = Instant.parse("2026-08-28T09:10:00Z")

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
    fun afterFajrSkipsSunriseAndFajr() {
        val alarms = remainingAthanAlarms(today, fajr.plusSeconds(1))
        assertEquals(
            listOf(PrayerName.DHUHR, PrayerName.ASR, PrayerName.MAGHRIB, PrayerName.ISHA),
            alarms.map { it.name },
        )
        assertFalse(alarms.any { it.name == PrayerName.SUNRISE })
        assertFalse(alarms.any { it.name == PrayerName.FAJR })
    }

    @Test
    fun afterIshaSchedulesTomorrowFajrOnly() {
        val afterIsha = today.copy(
            nextAthan = PrayerName.FAJR,
            nextAthanAt = tomorrowFajr,
        )
        val alarms = remainingAthanAlarms(afterIsha, isha.plusSeconds(1))
        assertEquals(listOf(PrayerInstant(PrayerName.FAJR, tomorrowFajr)), alarms)
    }

    @Test
    fun beforeFajrDoesNotDuplicateTomorrow() {
        val beforeFajr = today.copy(
            nextAthan = PrayerName.FAJR,
            nextAthanAt = fajr,
        )
        val alarms = remainingAthanAlarms(beforeFajr, fajr.minusSeconds(60))
        assertEquals(5, alarms.size)
        assertEquals(1, alarms.count { it.name == PrayerName.FAJR })
        assertTrue(alarms.none { it.name == PrayerName.SUNRISE })
    }
}
