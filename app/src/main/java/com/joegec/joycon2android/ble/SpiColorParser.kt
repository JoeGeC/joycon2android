package com.joegec.joycon2android.ble

/**
 * Extracts the Joy-Con 2 shell accent color from an SPI-flash read reply.
 *
 * The controller stores several colors in SPI flash. The body color at
 * 0x013019 is the near-black shell, identical across both Switch 2 Joy-Cons;
 * the accent color at [ACCENT_COLOR_ADDRESS] is the per-side colour (coral on
 * the right, blue on the left) that actually identifies a controller. We
 * request a read of the surrounding DeviceInfo block and pull the accent out
 * of the reply.
 *
 * Reply layout (command-response characteristic, little-endian), confirmed
 * against a live controller:
 *   [0]      report type (0x02 = SPI)
 *   [3]      command     (0x04 = SPI read)
 *   [8]      data length
 *   [12..15] source address (LE) — echoes the requested read address
 *   [16..]   data bytes, starting at the source address
 */
object SpiColorParser {

    private const val REPORT_TYPE_SPI = 0x02
    private const val COMMAND_SPI_READ = 0x04
    private const val ADDRESS_OFFSET = 0x0C
    private const val DATA_OFFSET = 0x10

    /** SPI flash address of the shell accent color, 3 bytes RGB. */
    const val ACCENT_COLOR_ADDRESS = 0x01301F

    /**
     * Returns the packed 0xRRGGBB accent color from an SPI-read reply, or null
     * if [reply] is not an SPI-read reply or doesn't span the accent address.
     */
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
