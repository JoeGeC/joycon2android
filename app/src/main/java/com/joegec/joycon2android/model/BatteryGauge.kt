package com.joegec.joycon2android.model

import kotlin.math.roundToInt

object BatteryGauge {
    // The BLE packet reports a regulated/under-load voltage ~0.6 V below the true cell
    // voltage (observed: ~3.30 V reads 75% on a Switch 2, ~3.60 V reads 100%). Anchors are
    // Nintendo's Joy-Con level thresholds (dekuNukem docs: 3.3/3.6/3.76/3.9/4.2 V) shifted
    // down 0.6 V to match. Below ~3.0 V is extrapolated — no low-battery readings observed yet.
    private val voltsToPercent = listOf(
        2.70f to 0,
        3.00f to 25,
        3.16f to 50,
        3.30f to 75,
        3.60f to 100,
    )

    fun percentFromVolts(volts: Float): Int {
        if (volts <= voltsToPercent.first().first) return 0
        if (volts >= voltsToPercent.last().first) return 100
        val upperIndex = voltsToPercent.indexOfFirst { (anchorVolts, _) -> volts < anchorVolts }
        val (lowVolts, lowPercent) = voltsToPercent[upperIndex - 1]
        val (highVolts, highPercent) = voltsToPercent[upperIndex]
        val fraction = (volts - lowVolts) / (highVolts - lowVolts)
        return (lowPercent + fraction * (highPercent - lowPercent)).roundToInt()
    }
}
