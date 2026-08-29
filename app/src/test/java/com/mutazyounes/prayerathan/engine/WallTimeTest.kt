package com.mutazyounes.prayerathan.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WallTimeTest {

    @Test
    fun fastDeviceWaitsToMatchTrueTime() {
        val prayer = 1_700_000_000_000L
        val correction = -500L
        assertEquals(prayer - 500L, WallTime.now(prayer, correction).toEpochMilli())
        assertEquals(prayer + 500L, WallTime.alarmEpochMilli(prayer, correction))
    }

    @Test
    fun slowDeviceFiresSoonerOnDeviceClock() {
        val prayer = 1_700_000_000_000L
        val correction = 400L
        assertEquals(prayer + 400L, WallTime.now(prayer, correction).toEpochMilli())
        assertEquals(prayer - 400L, WallTime.alarmEpochMilli(prayer, correction))
    }

    @Test
    fun nextSecondWait() {
        assertEquals(1000L, WallTime.millisUntilNextSecond(1_000L))
        assertEquals(250L, WallTime.millisUntilNextSecond(750L))
    }

    @Test
    fun ntpOffsetAveragesTrip() {
        val t1 = 1_000L
        val t2 = 1_200L
        val t3 = 1_200L
        val t4 = 1_400L
        assertEquals(0L, NtpMath.offsetMs(t1, t2, t3, t4))
    }

    @Test
    fun ntpTimestampRoundTrip() {
        val unixMs = 1_700_000_000_000L
        val packet = ByteArray(NtpMath.PACKET_SIZE)
        NtpMath.writeTimestamp(packet, 40, unixMs)
        assertEquals(unixMs, NtpMath.readTimestamp(packet, 40))
    }

    @Test
    fun rejectDayLongCorrection() {
        assertTrue(NtpMath.acceptCorrection(3_600_000L))
        assertFalse(NtpMath.acceptCorrection(NtpMath.MAX_ABS_CORRECTION_MS + 1))
    }
}
