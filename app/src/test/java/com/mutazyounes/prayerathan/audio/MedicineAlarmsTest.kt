package com.mutazyounes.prayerathan.audio

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MedicineAlarmsTest {

    private val zone: ZoneId = ZoneId.of("America/New_York")

    @Test
    fun remainingAlarmsSkipPastTimesAndWrongDays() {
        val monday = LocalDate.of(2026, 9, 14)
        val slot = MedicineSlot(
            id = "am",
            hour = 8,
            minute = 0,
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
        )
        val now = ZonedDateTime.of(monday, LocalTime.of(9, 0), zone).toInstant()
        val alarms = remainingMedicineAlarms(listOf(slot), now, zone, lookAheadDays = 4)
        assertEquals(1, alarms.size)
        assertEquals("am", alarms[0].slotId)
        val at = alarms[0].at.atZone(zone)
        assertEquals(DayOfWeek.WEDNESDAY, at.dayOfWeek)
        assertEquals(8, at.hour)
        assertEquals(0, at.minute)
    }

    @Test
    fun isMedicineMinuteMatchesSlot() {
        val slot = MedicineSlot(
            id = "pm",
            hour = 20,
            minute = 30,
            days = setOf(DayOfWeek.TUESDAY),
        )
        val hit = ZonedDateTime.of(LocalDate.of(2026, 9, 15), LocalTime.of(20, 30), zone).toInstant()
        val miss = ZonedDateTime.of(LocalDate.of(2026, 9, 15), LocalTime.of(20, 34), zone).toInstant()
        assertTrue(isMedicineMinute(listOf(slot), hit, zone))
        assertFalse(isMedicineMinute(listOf(slot), miss, zone))
        assertEquals(slot, medicineSlotFor(listOf(slot), hit, zone))
        assertNull(medicineSlotFor(listOf(slot), miss, zone))
    }

    @Test
    fun graceAllowsLateAlarmWithinThreeMinutes() {
        val slot = MedicineSlot(
            id = "nine",
            hour = 21,
            minute = 0,
            days = setOf(DayOfWeek.TUESDAY),
        )
        val late = ZonedDateTime.of(LocalDate.of(2026, 9, 15), LocalTime.of(21, 2), zone).toInstant()
        assertNotNull(medicineSlotFor(listOf(slot), late, zone))
        assertTrue(isMedicineMinute(listOf(slot), late, zone))
    }

    @Test
    fun graceRejectsBeforeSlot() {
        val slot = MedicineSlot(
            id = "nine",
            hour = 21,
            minute = 0,
            days = setOf(DayOfWeek.TUESDAY),
        )
        val early = ZonedDateTime.of(LocalDate.of(2026, 9, 15), LocalTime.of(20, 59), zone).toInstant()
        assertNull(medicineSlotFor(listOf(slot), early, zone))
    }

    @Test
    fun fireKeyIsStablePerOccurrence() {
        val slot = MedicineSlot(
            id = "abc",
            hour = 9,
            minute = 0,
            days = DayOfWeek.entries.toSet(),
        )
        assertEquals("abc|2026-09-15|9|0", medicineFireKey(slot, LocalDate.of(2026, 9, 15)))
    }

    @Test
    fun encodeDecodeRoundTrip() {
        val slot = MedicineSlot(
            id = "abc12",
            hour = 7,
            minute = 15,
            days = setOf(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
        )
        val raw = MedicineSettingsStore.encode(slot)
        val decoded = MedicineSettingsStore.decode(raw)
        assertEquals(slot, decoded)
    }

    @Test
    fun decodeRejectsEmptyDays() {
        assertNull(MedicineSettingsStore.decode("x|8|0|0000000"))
    }

    @Test
    fun emptySlotsYieldNoAlarms() {
        val now = Instant.parse("2026-09-15T12:00:00Z")
        assertTrue(remainingMedicineAlarms(emptyList(), now, zone).isEmpty())
    }
}
