package com.joegec.joycon2android.dsu

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

/**
 * Parses client → server DSU packets: magic "DSUC", then the same header layout the
 * encoder writes. Packets with a bad magic, length, or CRC are dropped (returns null).
 */
object DsuRequestParser {

    private const val HEADER_SIZE = 16
    private const val MIN_PACKET_SIZE = HEADER_SIZE + 4
    private const val CRC_OFFSET = 8
    private val CLIENT_MAGIC = "DSUC".toByteArray(Charsets.US_ASCII)

    private const val TYPE_VERSION = 0x100000
    private const val TYPE_PORT_INFO = 0x100001
    private const val TYPE_PAD_DATA = 0x100002

    fun parse(data: ByteArray, length: Int): DsuRequest? {
        if (length < MIN_PACKET_SIZE) return null
        if (!hasClientMagic(data)) return null
        if (!crcMatches(data, length)) return null

        val packet = ByteBuffer.wrap(data, 0, length).order(ByteOrder.LITTLE_ENDIAN)
        return when (packet.getInt(HEADER_SIZE)) {
            TYPE_VERSION -> DsuRequest.Version
            TYPE_PORT_INFO -> parsePortInfo(packet, length)
            TYPE_PAD_DATA -> parsePadData(packet, length)
            else -> null
        }
    }

    private fun parsePadData(packet: ByteBuffer, length: Int): DsuRequest.PadData? {
        if (length < MIN_PACKET_SIZE + 8) return null
        return DsuRequest.PadData(
            flags = packet.get(MIN_PACKET_SIZE).toInt() and 0xFF,
            slot = packet.get(MIN_PACKET_SIZE + 1).toInt() and 0xFF,
        )
    }

    private fun hasClientMagic(data: ByteArray): Boolean =
        CLIENT_MAGIC.indices.all { data[it] == CLIENT_MAGIC[it] }

    private fun crcMatches(data: ByteArray, length: Int): Boolean {
        val stored = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).getInt(CRC_OFFSET)
        val crc = CRC32().apply {
            update(data, 0, CRC_OFFSET)
            update(ByteArray(4))
            update(data, CRC_OFFSET + 4, length - CRC_OFFSET - 4)
        }
        return crc.value.toInt() == stored
    }

    private fun parsePortInfo(packet: ByteBuffer, length: Int): DsuRequest.PortInfo? {
        val count = packet.getInt(MIN_PACKET_SIZE)
        if (count !in 1..DsuPacketEncoder.SLOT_COUNT) return null
        if (length < MIN_PACKET_SIZE + 4 + count) return null
        val slots = List(count) { packet.get(MIN_PACKET_SIZE + 4 + it).toInt() }
        return DsuRequest.PortInfo(slots)
    }
}
