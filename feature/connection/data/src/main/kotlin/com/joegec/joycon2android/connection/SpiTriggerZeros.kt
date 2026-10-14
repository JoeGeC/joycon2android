package com.joegec.joycon2android.connection

/** Factory trigger zero points, left then right: docs/protocol.md#analog-triggers */
object SpiTriggerZeros {

    const val ADDRESS = 0x013140
    const val LENGTH = 2
    private const val UNSET = 0xFF

    /** Null when [reply] isn't this read; a null zero when flash leaves it unset. */
    fun parse(reply: ByteArray): Pair<Int?, Int?>? {
        val zeros = SpiReadReply.bytesAt(reply, ADDRESS, LENGTH) ?: return null
        return zero(zeros[0]) to zero(zeros[1])
    }

    private fun zero(byte: Byte): Int? = (byte.toInt() and 0xFF).takeIf { it != UNSET }
}
