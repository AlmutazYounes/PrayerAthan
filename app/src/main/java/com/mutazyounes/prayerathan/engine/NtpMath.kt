package com.mutazyounes.prayerathan.engine

object NtpMath {
    const val PACKET_SIZE = 48
    const val UNIX_OFFSET_SECONDS = 2_208_988_800L
    const val MAX_ABS_CORRECTION_MS = 24L * 60L * 60L * 1000L

    fun timestampToUnixMs(seconds: Long, fraction: Long): Long {
        val unixSeconds = seconds - UNIX_OFFSET_SECONDS
        val millis = (fraction * 1000L) ushr 32
        return unixSeconds * 1000L + millis
    }

    fun unixMsToNtpSeconds(unixMs: Long): Long =
        (unixMs / 1000L) + UNIX_OFFSET_SECONDS

    fun unixMsToNtpFraction(unixMs: Long): Long {
        val millis = unixMs % 1000L
        return (millis shl 32) / 1000L
    }

    fun readTimestamp(packet: ByteArray, offset: Int): Long {
        val seconds = readU32(packet, offset)
        val fraction = readU32(packet, offset + 4)
        return timestampToUnixMs(seconds, fraction)
    }

    fun writeTimestamp(packet: ByteArray, offset: Int, unixMs: Long) {
        writeU32(packet, offset, unixMsToNtpSeconds(unixMs))
        writeU32(packet, offset + 4, unixMsToNtpFraction(unixMs))
    }

    fun offsetMs(t1: Long, t2: Long, t3: Long, t4: Long): Long =
        ((t2 - t1) + (t3 - t4)) / 2L

    fun acceptCorrection(correctionMs: Long): Boolean =
        kotlin.math.abs(correctionMs) <= MAX_ABS_CORRECTION_MS

    private fun readU32(packet: ByteArray, offset: Int): Long {
        return ((packet[offset].toLong() and 0xffL) shl 24) or
            ((packet[offset + 1].toLong() and 0xffL) shl 16) or
            ((packet[offset + 2].toLong() and 0xffL) shl 8) or
            (packet[offset + 3].toLong() and 0xffL)
    }

    private fun writeU32(packet: ByteArray, offset: Int, value: Long) {
        packet[offset] = (value ushr 24).toByte()
        packet[offset + 1] = (value ushr 16).toByte()
        packet[offset + 2] = (value ushr 8).toByte()
        packet[offset + 3] = value.toByte()
    }
}
