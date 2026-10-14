package com.joegec.joycon2android.connection

/** Pulls the shell accent colour out of an SPI-flash read reply: docs/protocol.md#spi-reads. */
object SpiColorParser {

    const val ACCENT_COLOR_ADDRESS = 0x01301F

    /** Packed 0xRRGGBB, or null if this is not an SPI read or does not span the accent address. */
    fun parseAccentColor(reply: ByteArray): Int? {
        val rgb = SpiReadReply.bytesAt(reply, ACCENT_COLOR_ADDRESS, 3) ?: return null
        return ((rgb[0].toInt() and 0xFF) shl 16) or ((rgb[1].toInt() and 0xFF) shl 8) or (rgb[2].toInt() and 0xFF)
    }
}
