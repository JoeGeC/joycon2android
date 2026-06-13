package com.joegec.joycon2android.dsu

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DsuRequestParserTest {

    @Test
    fun `parses a version request`() {
        val packet = clientPacket(0x100000)

        assertEquals(DsuRequest.Version, DsuRequestParser.parse(packet, packet.size))
    }

    @Test
    fun `parses a port info request with its slots`() {
        val payload = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
            .putInt(2).put(0).put(1).array()
        val packet = clientPacket(0x100001, payload)

        assertEquals(DsuRequest.PortInfo(listOf(0, 1)), DsuRequestParser.parse(packet, packet.size))
    }

    @Test
    fun `parses a pad data subscription with its flags and slot`() {
        val payload = ByteArray(8).also { it[0] = 1; it[1] = 2 }
        val packet = clientPacket(0x100002, payload)

        assertEquals(DsuRequest.PadData(flags = 1, slot = 2), DsuRequestParser.parse(packet, packet.size))
    }

    @Test
    fun `rejects a truncated pad data subscription`() {
        val packet = clientPacket(0x100002, ByteArray(4))

        assertEquals(null, DsuRequestParser.parse(packet, packet.size))
    }

    @Test
    fun `rejects a wrong magic`() {
        val packet = clientPacket(0x100000)
        packet[0] = 'X'.code.toByte()

        assertNull(DsuRequestParser.parse(packet, packet.size))
    }

    @Test
    fun `rejects a corrupted CRC`() {
        val packet = clientPacket(0x100000)
        packet[8] = (packet[8] + 1).toByte()

        assertNull(DsuRequestParser.parse(packet, packet.size))
    }

    @Test
    fun `rejects truncated packets`() {
        assertNull(DsuRequestParser.parse(ByteArray(10), 10))
    }

    @Test
    fun `rejects an unknown message type`() {
        val packet = clientPacket(0x123456)

        assertNull(DsuRequestParser.parse(packet, packet.size))
    }

    @Test
    fun `rejects a port info request with an out-of-range count`() {
        val payload = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(9).array()
        val packet = clientPacket(0x100001, payload)

        assertNull(DsuRequestParser.parse(packet, packet.size))
    }

    private fun clientPacket(type: Int, payload: ByteArray = ByteArray(0)): ByteArray {
        val packet = ByteBuffer.allocate(20 + payload.size).order(ByteOrder.LITTLE_ENDIAN)
            .put("DSUC".toByteArray(Charsets.US_ASCII))
            .putShort(1001)
            .putShort((4 + payload.size).toShort())
            .putInt(0)
            .putInt(0x0BADCAFE.toInt())
            .putInt(type)
            .put(payload)
            .array()
        val crc = CRC32().apply { update(packet) }.value.toInt()
        ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN).putInt(8, crc)
        return packet
    }
}
