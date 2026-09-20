package com.mutazyounes.prayerathan.audio

import android.media.AudioDeviceInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AthanOutputTest {

    @Test
    fun picksBuiltinSpeakerOverHeadsetAndBluetooth() {
        val types = intArrayOf(
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
        )
        assertEquals(
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,
            AthanOutput.preferredType(types),
        )
    }

    @Test
    fun skipsWhenNoBuiltinSpeaker() {
        val types = intArrayOf(
            AudioDeviceInfo.TYPE_WIRED_HEADSET,
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        )
        assertNull(AthanOutput.preferredType(types))
    }
}
