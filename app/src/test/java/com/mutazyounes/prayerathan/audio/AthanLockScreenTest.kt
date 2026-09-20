package com.mutazyounes.prayerathan.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AthanLockScreenTest {

    @Test
    fun liveAthanUsesFullScreenAndNewHighChannel() {
        assertTrue(AthanLockScreen.usesFullScreen(true))
        assertEquals("athan_playback_alarm", AthanLockScreen.CHANNEL_ID)
    }

    @Test
    fun demoDoesNotUseFullScreen() {
        assertFalse(AthanLockScreen.usesFullScreen(false))
    }
}
