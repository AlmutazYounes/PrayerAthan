package com.mutazyounes.prayerathan.audio

import com.mutazyounes.prayerathan.engine.PrayerName
import org.junit.Assert.assertEquals
import org.junit.Test

class AthanVolumeTest {

    @Test
    fun fullVolumeIsUnityGain() {
        assertEquals(1f, AthanVolume.gain(100), 0.0001f)
    }

    @Test
    fun silentIsZeroGain() {
        assertEquals(0f, AthanVolume.gain(0), 0f)
    }

    @Test
    fun halfIsLinear() {
        assertEquals(0.5f, AthanVolume.gain(50), 0.0001f)
    }

    @Test
    fun clampsAboveMax() {
        assertEquals(1f, AthanVolume.gain(150), 0f)
        assertEquals(100, AthanVolume.clamp(150))
    }

    @Test
    fun clampsBelowMin() {
        assertEquals(0f, AthanVolume.gain(-20), 0f)
        assertEquals(0, AthanVolume.clamp(-20))
    }

    @Test
    fun previewKeyIsPerPrayer() {
        assertEquals("volume:FAJR", AthanVolume.demoKey(PrayerName.FAJR))
        assertEquals("volume:ISHA", AthanVolume.demoKey(PrayerName.ISHA))
    }
}
