package com.joegec.joycon2android.connection

/** 9 bytes: centre, span above, span below, each an X/Y pair packed in 12 bits. docs/protocol.md#spi-reads */
object SpiStickCalibration {

    const val MAIN_ADDRESS = 0x0130A8
    const val RIGHT_ADDRESS = 0x0130E8
    const val LENGTH = 9

    // Unset flash reads 0xFFF; anything outside these is not a stick this app knows.
    private val CENTRE_RANGE = 1024..3072
    private val SPAN_RANGE = 500..2000

    /** Null when [reply] isn't a read of [address], or the flash there is unset or implausible. */
    fun parse(reply: ByteArray, address: Int): StickCalibration? {
        val bytes = SpiReadReply.bytesAt(reply, address, LENGTH) ?: return null
        val (centreX, centreY) = pair(bytes, 0)
        val (aboveX, aboveY) = pair(bytes, 3)
        val (belowX, belowY) = pair(bytes, 6)
        return StickCalibration(centreX, centreY, aboveX, aboveY, belowX, belowY).takeIf(::plausible)
    }

    private fun pair(bytes: ByteArray, offset: Int): Pair<Int, Int> {
        val b0 = bytes[offset].toInt() and 0xFF
        val b1 = bytes[offset + 1].toInt() and 0xFF
        val b2 = bytes[offset + 2].toInt() and 0xFF
        return (b0 or ((b1 and 0x0F) shl 8)) to ((b1 shr 4) or (b2 shl 4))
    }

    private fun plausible(c: StickCalibration): Boolean =
        c.centreX in CENTRE_RANGE && c.centreY in CENTRE_RANGE &&
            listOf(c.aboveX, c.aboveY, c.belowX, c.belowY).all { it in SPAN_RANGE }
}
