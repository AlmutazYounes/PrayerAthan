package com.mutazyounes.prayerathan.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrayerMethodTest {

    @Test
    fun settingsLineNamesIsnaAndShafi() {
        assertEquals("ISNA, North America", PrayerMethod.METHOD)
        assertEquals("Shafi", PrayerMethod.MADHAB)
        assertEquals("Times: ISNA, North America. Asr is Shafi.", PrayerMethod.settingsLine())
    }
}
