package com.mutazyounes.prayerathan.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AthanVolumeKeysTest {

    @Test
    fun longPressStops() {
        assertTrue(AthanVolumeKeys.stopAfterDown(repeatCount = 1, streamVolume = 7))
    }

    @Test
    fun zeroVolumeStops() {
        assertTrue(AthanVolumeKeys.stopAfterDown(repeatCount = 0, streamVolume = 0))
    }

    @Test
    fun firstTapWithVolumeLeftDoesNotStop() {
        assertFalse(AthanVolumeKeys.stopAfterDown(repeatCount = 0, streamVolume = 4))
    }
}
