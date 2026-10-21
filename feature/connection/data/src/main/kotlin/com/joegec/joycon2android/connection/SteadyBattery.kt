package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.BatteryCharge
import kotlin.math.abs

/** Holds the charge until a reading moves past the voltage's noise: docs/protocol.md#battery */
internal class SteadyBattery {
    private var shown: BatteryCharge? = null

    fun steady(reading: BatteryCharge?): BatteryCharge? {
        val current = shown
        if (reading == null || current == null || abs(reading.percent - current.percent) >= NOISE_PERCENT) {
            shown = reading
        }
        return shown
    }

    fun reset() {
        shown = null
    }

    private companion object {
        const val NOISE_PERCENT = 5
    }
}
