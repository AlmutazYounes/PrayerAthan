package com.mutazyounes.prayerathan.shell

import com.mutazyounes.prayerathan.engine.NtpMath
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object NtpClient {
    private val HOSTS = listOf("time.google.com", "time.cloudflare.com", "pool.ntp.org")

    fun queryCorrectionMs(): Long? {
        for (host in HOSTS) {
            queryHost(host)?.let { return it }
        }
        return null
    }

    internal fun queryHost(host: String, timeoutMs: Int = 3_000): Long? {
        return try {
            DatagramSocket().use { socket ->
                socket.soTimeout = timeoutMs
                val request = ByteArray(NtpMath.PACKET_SIZE)
                request[0] = 0x1B
                val t1 = System.currentTimeMillis()
                NtpMath.writeTimestamp(request, 40, t1)
                val address = InetAddress.getByName(host)
                socket.send(DatagramPacket(request, request.size, address, 123))
                val reply = ByteArray(NtpMath.PACKET_SIZE)
                socket.receive(DatagramPacket(reply, reply.size))
                val t4 = System.currentTimeMillis()
                parseCorrection(reply, t1, t4)
            }
        } catch (_: Exception) {
            null
        }
    }

    internal fun parseCorrection(packet: ByteArray, t1: Long, t4: Long): Long? {
        if (packet.size < NtpMath.PACKET_SIZE) return null
        val mode = packet[0].toInt() and 0x07
        if (mode != 4) return null
        val t2 = NtpMath.readTimestamp(packet, 32)
        val t3 = NtpMath.readTimestamp(packet, 40)
        if (t2 == 0L || t3 == 0L) return null
        val correction = NtpMath.offsetMs(t1, t2, t3, t4)
        return if (NtpMath.acceptCorrection(correction)) correction else null
    }
}
