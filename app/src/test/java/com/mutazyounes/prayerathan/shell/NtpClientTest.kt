package com.mutazyounes.prayerathan.shell

import com.mutazyounes.prayerathan.engine.NtpMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NtpClientTest {

    @Test
    fun parseServerModeFour() {
        val packet = ByteArray(NtpMath.PACKET_SIZE)
        packet[0] = 0x24
        NtpMath.writeTimestamp(packet, 32, 2_000L)
        NtpMath.writeTimestamp(packet, 40, 2_000L)
        assertEquals(0L, NtpClient.parseCorrection(packet, 1_000L, 3_000L))
    }

    @Test
    fun rejectClientMode() {
        val packet = ByteArray(NtpMath.PACKET_SIZE)
        packet[0] = 0x1B
        NtpMath.writeTimestamp(packet, 32, 2_000L)
        NtpMath.writeTimestamp(packet, 40, 2_010L)
        assertNull(NtpClient.parseCorrection(packet, 1_000L, 3_000L))
    }
}
