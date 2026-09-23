package com.mutazyounes.prayerathan.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsTimeTest {

    @Test
    fun offsetZeroReadsAsIsna() {
        assertEquals("ISNA", SettingsTime.offsetLabel(0))
    }

    @Test
    fun offsetShowsSignedMinutes() {
        assertEquals("+3 min", SettingsTime.offsetLabel(3))
        assertEquals("-8 min", SettingsTime.offsetLabel(-8))
    }

    @Test
    fun clock12FormatsNoonAndMidnight() {
        assertEquals("12:00 AM", SettingsTime.clock12(0, 0))
        assertEquals("12:00 PM", SettingsTime.clock12(12, 0))
        assertEquals("5:15 AM", SettingsTime.clock12(5, 15))
        assertEquals("6:03 PM", SettingsTime.clock12(18, 3))
    }

    @Test
    fun hour24RoundTrip() {
        for (hour in 0..23) {
            val back = SettingsTime.hour24(SettingsTime.hour12(hour), SettingsTime.isAm(hour))
            assertEquals(hour, back)
        }
        assertTrue(SettingsTime.isAm(0))
        assertFalse(SettingsTime.isAm(12))
    }

    @Test
    fun wheelsWrap() {
        assertEquals(12, SettingsTime.wrapHour12(0))
        assertEquals(1, SettingsTime.wrapHour12(13))
        assertEquals(59, SettingsTime.wrapMinute(-1))
        assertEquals(0, SettingsTime.wrapMinute(60))
    }
}
