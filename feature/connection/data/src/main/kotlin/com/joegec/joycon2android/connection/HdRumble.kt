package com.joegec.joycon2android.connection

/** Packets for the vibration characteristic: docs/protocol.md#hd-rumble */
internal object HdRumble {

    const val MAX_AMPLITUDE = 0x3FF

    private const val REPORT_ID: Byte = 0x00
    private const val HEADER = 0x50
    private const val SUBFRAMES = 3
    private const val SUBFRAME_LENGTH = 5

    // SDL's carrier frequencies, high then low.
    private const val HIGH_FREQUENCY = 0x187L
    private const val LOW_FREQUENCY = 0x112L

    /** [motors] is 2 for a Pro Controller, which takes its left and right groups in one packet. */
    fun packet(counter: Int, amplitude: Int, motors: Int = 1): ByteArray {
        val group = group(counter, amplitude)
        return byteArrayOf(REPORT_ID) + ByteArray(group.size * motors) { group[it % group.size] }
    }

    private fun group(counter: Int, amplitude: Int): ByteArray {
        val subframe = subframe(amplitude)
        return byteArrayOf((HEADER or (counter and 0x0F)).toByte()) +
            ByteArray(SUBFRAMES * SUBFRAME_LENGTH) { subframe[it % SUBFRAME_LENGTH] }
    }

    private fun subframe(amplitude: Int): ByteArray {
        val amp = amplitude.coerceIn(0, MAX_AMPLITUDE).toLong()
        val packed = HIGH_FREQUENCY or (amp shl 10) or (LOW_FREQUENCY shl 20) or (amp shl 30)
        return ByteArray(SUBFRAME_LENGTH) { (packed shr (8 * it)).toByte() }
    }
}
