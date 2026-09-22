package com.joegec.joycon2android.connection

/** Pulls the shell accent colour out of an SPI-flash read reply: docs/protocol.md#spi-reads. */
object SpiColorParser {

    private const val REPORT_TYPE_SPI = 0x02
    private const val COMMAND_SPI_READ = 0x04
    private const val ADDRESS_OFFSET = 0x0C
    private const val DATA_OFFSET = 0x10

    const val ACCENT_COLOR_ADDRESS = 0x01301F

    /** Packed 0xRRGGBB, or null if this is not an SPI read or does not span the accent address. */
    fun parseAccentColor(reply: ByteArray): Int? {
        if (reply.size < DATA_OFFSET) return null
        if (reply[0].toInt() and 0xFF != REPORT_TYPE_SPI) return null
        if (reply[3].toInt() and 0xFF != COMMAND_SPI_READ) return null

        val baseAddress = readLeUInt32(reply, ADDRESS_OFFSET)
        val colorOffset = DATA_OFFSET + (ACCENT_COLOR_ADDRESS - baseAddress)
        if (colorOffset < DATA_OFFSET || colorOffset + 3 > reply.size) return null

        val r = reply[colorOffset].toInt() and 0xFF
        val g = reply[colorOffset + 1].toInt() and 0xFF
        val b = reply[colorOffset + 2].toInt() and 0xFF
        return (r shl 16) or (g shl 8) or b
    }

    private fun readLeUInt32(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
            ((data[offset + 1].toInt() and 0xFF) shl 8) or
            ((data[offset + 2].toInt() and 0xFF) shl 16) or
            ((data[offset + 3].toInt() and 0xFF) shl 24)
    }
}
