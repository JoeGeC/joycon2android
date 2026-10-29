package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Rumble
import kotlin.math.roundToInt

/** Packets for the vibration characteristic: docs/protocol.md#hd-rumble */
internal object HdRumble {

    const val MAX_AMPLITUDE = 0x3FF

    // Strength 1: SDL's cap, 29000 of 65535, so the actuator isn't overdriven. docs/protocol.md#hd-rumble
    private const val SAFE_AMPLITUDE = MAX_AMPLITUDE * 29000 / 65535

    private const val REPORT_ID: Byte = 0x00
    private const val HEADER = 0x50
    private const val SUBFRAMES = 3
    private const val SUBFRAME_LENGTH = 5

    // SDL's carrier frequencies.
    private const val HIGH_FREQUENCY = 0x187L
    private const val LOW_FREQUENCY = 0x112L

    /** The strong motor drives the low band, the weak one the high band, as SDL maps them. */
    fun packet(counter: Int, rumble: Rumble, motors: Int = 1): ByteArray =
        packet(counter, high = amplitude(rumble.weak), low = amplitude(rumble.strong), motors = motors)

    /** [motors] is 2 for a Pro Controller, which takes its left and right groups in one packet. */
    fun packet(counter: Int, high: Int, low: Int, motors: Int = 1): ByteArray {
        val group = group(counter, high, low)
        return byteArrayOf(REPORT_ID) + ByteArray(group.size * motors) { group[it % group.size] }
    }

    private fun amplitude(strength: Float): Int = (strength.coerceIn(0f, 1f) * SAFE_AMPLITUDE).roundToInt()

    private fun group(counter: Int, high: Int, low: Int): ByteArray {
        val subframe = subframe(high, low)
        return byteArrayOf((HEADER or (counter and 0x0F)).toByte()) +
            ByteArray(SUBFRAMES * SUBFRAME_LENGTH) { subframe[it % SUBFRAME_LENGTH] }
    }

    private fun subframe(high: Int, low: Int): ByteArray {
        val packed = HIGH_FREQUENCY or (high.band() shl 10) or (LOW_FREQUENCY shl 20) or (low.band() shl 30)
        return ByteArray(SUBFRAME_LENGTH) { (packed shr (8 * it)).toByte() }
    }

    private fun Int.band(): Long = coerceIn(0, MAX_AMPLITUDE).toLong()
}
