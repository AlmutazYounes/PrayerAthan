package com.mutazyounes.prayerathan.audio

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MorningAthkarAlarmsTest {

    private val zone = ZoneId.of("America/New_York")
    private val date = LocalDate.of(2026, 8, 27)

    private fun at(hour: Int, minute: Int): Instant =
        date.atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant()

    @Test
    fun slotTimesAreEightOhFivePlusFiveMinuteSteps() {
        val times = morningAthkarLocalTimes(6)
        assertEquals(
            listOf(
                LocalTime.of(8, 5),
                LocalTime.of(8, 10),
                LocalTime.of(8, 15),
                LocalTime.of(8, 20),
                LocalTime.of(8, 25),
                LocalTime.of(8, 30),
            ),
            times,
        )
    }

    @Test
    fun beforeSequenceArmsAllSixToday() {
        val slots = remainingMorningAthkarAlarms(at(7, 0), zone, clipCount = 6)
        assertEquals(6, slots.size)
        assertEquals(at(8, 5), slots.first().at)
        assertEquals(0, slots.first().index)
        assertEquals(at(8, 30), slots.last().at)
        assertEquals(5, slots.last().index)
    }

    @Test
    fun midSequenceArmsRemainingOnly() {
        val slots = remainingMorningAthkarAlarms(at(8, 12), zone, clipCount = 6)
        assertEquals(listOf(2, 3, 4, 5), slots.map { it.index })
        assertEquals(at(8, 15), slots.first().at)
    }

    @Test
    fun afterSequenceArmsTomorrowMorning() {
        val slots = remainingMorningAthkarAlarms(at(9, 0), zone, clipCount = 6)
        assertEquals(6, slots.size)
        assertEquals(0, slots.first().index)
        val tomorrow = date.plusDays(1).atTime(8, 5).atZone(zone).toInstant()
        assertEquals(tomorrow, slots.first().at)
    }

    @Test
    fun disabledCallerGetsEmptyWhenClipCountZero() {
        assertTrue(remainingMorningAthkarAlarms(at(7, 0), zone, clipCount = 0).isEmpty())
    }

    @Test
    fun skipsSlotWhenAthanOwnsThatMinute() {
        val athan = listOf(at(8, 10))
        val slots = remainingMorningAthkarAlarms(
            now = at(7, 0),
            zone = zone,
            clipCount = 6,
            athanTimes = athan,
        )
        assertFalse(slots.any { it.index == 1 })
        assertTrue(slots.any { it.index == 0 })
        assertTrue(slots.any { it.index == 2 })
    }

    @Test
    fun skipsSlotWhenMedicineOwnsThatMinute() {
        val slot = MedicineSlot(
            id = "am",
            hour = 8,
            minute = 5,
            days = setOf(DayOfWeek.THURSDAY),
        )
        val slots = remainingMorningAthkarAlarms(
            now = at(7, 0),
            zone = zone,
            clipCount = 6,
            medicineSlots = listOf(slot),
        )
        assertFalse(slots.any { it.index == 0 })
        assertTrue(slots.any { it.index == 1 })
    }

    @Test
    fun yieldsToAthanAndMedicine() {
        assertFalse(
            shouldPlayMorningAthkar(
                morningEnabled = true,
                athanPlaying = true,
                medicinePlaying = false,
                athanMinute = false,
                medicineDue = false,
            ),
        )
        assertFalse(
            shouldPlayMorningAthkar(
                morningEnabled = true,
                athanPlaying = false,
                medicinePlaying = false,
                athanMinute = false,
                medicineDue = true,
            ),
        )
        assertFalse(
            shouldPlayMorningAthkar(
                morningEnabled = false,
                athanPlaying = false,
                medicinePlaying = false,
                athanMinute = false,
                medicineDue = false,
            ),
        )
        assertTrue(
            shouldPlayMorningAthkar(
                morningEnabled = true,
                athanPlaying = false,
                medicinePlaying = false,
                athanMinute = false,
                medicineDue = false,
            ),
        )
    }
}
