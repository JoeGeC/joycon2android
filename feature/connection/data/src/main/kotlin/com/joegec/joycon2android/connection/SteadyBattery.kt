package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.BatteryCharge
import kotlin.math.abs

/** Median of recent readings, held above 5% until it moves past the voltage's noise: docs/protocol.md#battery */
internal class SteadyBattery {
    private val recent = ArrayDeque<Int>()
    private var shown: BatteryCharge? = null

    fun steady(reading: BatteryCharge?): BatteryCharge? {
        if (reading == null) return reset()
        remember(reading.percent)
        if (recent.size < WINDOW) return shown
        val median = BatteryCharge(recent.sorted()[WINDOW / 2])
        if (!heldAgainst(median)) shown = median
        return shown
    }

    private fun heldAgainst(median: BatteryCharge): Boolean {
        val current = shown ?: return false
        return median.percent >= HELD_FROM_PERCENT && abs(median.percent - current.percent) < NOISE_PERCENT
    }

    fun reset(): BatteryCharge? {
        recent.clear()
        shown = null
        return null
    }

    private fun remember(percent: Int) {
        recent.addLast(percent)
        if (recent.size > WINDOW) recent.removeFirst()
    }

    private companion object {
        // Reports, ~1–2 s at 33–67 Hz.
        const val WINDOW = 64
        const val NOISE_PERCENT = 5
        const val HELD_FROM_PERCENT = 5
    }
}
