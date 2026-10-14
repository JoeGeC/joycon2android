package com.joegec.joycon2android.connection

/** docs/protocol.md#spi-reads */
object SpiReadReply {

    private const val REPORT_TYPE_SPI = 0x02
    private const val COMMAND_SPI_READ = 0x04
    private const val ADDRESS_OFFSET = 0x0C
    private const val DATA_OFFSET = 0x10

    /** The [length] bytes at [address], or null if [reply] is not an SPI read that spans them. */
    fun bytesAt(reply: ByteArray, address: Int, length: Int): ByteArray? {
        if (reply.size < DATA_OFFSET) return null
        if (reply[0].toInt() and 0xFF != REPORT_TYPE_SPI) return null
        if (reply[3].toInt() and 0xFF != COMMAND_SPI_READ) return null

        val start = DATA_OFFSET + (address - readLeUInt32(reply, ADDRESS_OFFSET))
        if (start < DATA_OFFSET || start + length > reply.size) return null
        return reply.copyOfRange(start, start + length)
    }

    private fun readLeUInt32(data: ByteArray, offset: Int): Int =
        (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16) or
            ((data[offset + 3].toInt() and 0xFF) shl 24)
}
