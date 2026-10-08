package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import kotlin.math.abs

/** Raw trigger bytes onto 0..255, full at the first stop: docs/protocol.md#analog-triggers */
class TriggerCalibrator(
    seedFull: Int = DEFAULT_SEED_FULL,
    deadZone: Int = DEFAULT_DEAD_ZONE,
) {

    private val left = Axis(seedFull, deadZone)
    private val right = Axis(seedFull, deadZone)

    fun setFactoryZeros(left: Int?, right: Int?) {
        this.left.factoryZero = left
        this.right.factoryZero = right
    }

    fun calibrate(input: JoyconInput): JoyconInput = input.copy(
        triggerLeft = left.rescale(input.triggerLeft, clicked = JoyconButton.L.id in input.pressed),
        triggerRight = right.rescale(input.triggerRight, clicked = JoyconButton.R.id in input.pressed),
    )

    private class Axis(private var full: Int, private val deadZone: Int) {
        var factoryZero: Int? = null
        private var lowestSeen = Int.MAX_VALUE
        private var heldValue = -1
        private var heldPackets = 0

        fun rescale(raw: Int, clicked: Boolean): Int {
            lowestSeen = minOf(lowestSeen, raw)
            if (!clicked) learnFull(raw)
            val start = (factoryZero ?: lowestSeen) + deadZone
            if (raw <= start) return 0
            return ((raw - start) * MAX / (full - start).coerceAtLeast(1)).coerceAtMost(MAX)
        }

        // Only a held value counts: a quick press passes far beyond the first stop on its way to the click.
        private fun learnFull(raw: Int) {
            if (abs(raw - heldValue) <= HOLD_JITTER) heldPackets++ else {
                heldValue = raw
                heldPackets = 1
            }
            if (heldPackets >= HOLD_PACKETS && raw in (full + 1)..FULL_CEILING) full = raw
        }
    }

    companion object {
        private const val MAX = 255

        // Measured on three units (2026-10-07): first stops 170-196, rest up to 4 above the factory zero.
        private const val DEFAULT_SEED_FULL = 170
        private const val DEFAULT_DEAD_ZONE = 5
        private const val FULL_CEILING = 205

        // ~150 ms at the 30 ms balanced interval.
        private const val HOLD_PACKETS = 5
        private const val HOLD_JITTER = 2
    }
}
