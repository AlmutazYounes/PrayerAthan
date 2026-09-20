package com.mutazyounes.prayerathan.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class PrayerOffsetsTest {

    @Test
    fun defaultIsZeroForEveryPrayer() {
        PrayerOffsets.ALL.forEach { name ->
            assertEquals(0, PrayerOffsets.ZERO.minutesOf(name))
        }
    }

    @Test
    fun clampCapsAtSixty() {
        assertEquals(60, PrayerOffsets.clamp(90))
        assertEquals(-60, PrayerOffsets.clamp(-90))
    }

    @Test
    fun withReplacesOnePrayerOnly() {
        val next = PrayerOffsets.ZERO.with(PrayerName.FAJR, -15).with(PrayerName.ISHA, 15)
        assertEquals(-15, next.minutesOf(PrayerName.FAJR))
        assertEquals(15, next.minutesOf(PrayerName.ISHA))
        assertEquals(0, next.minutesOf(PrayerName.MAGHRIB))
    }
}
