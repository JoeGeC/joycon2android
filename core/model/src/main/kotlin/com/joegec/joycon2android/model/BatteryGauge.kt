package com.joegec.joycon2android.model

import kotlin.math.roundToInt

object BatteryGauge {
    // Anchors and the 0.6 V offset: docs/protocol.md#battery
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

    /** Inverse of [percentFromVolts], for controllers that report a charge level instead of a voltage. */
    fun voltsFromPercent(percent: Int): Float {
        if (percent <= voltsToPercent.first().second) return voltsToPercent.first().first
        if (percent >= voltsToPercent.last().second) return voltsToPercent.last().first
        val upperIndex = voltsToPercent.indexOfFirst { (_, anchorPercent) -> percent < anchorPercent }
        val (lowVolts, lowPercent) = voltsToPercent[upperIndex - 1]
        val (highVolts, highPercent) = voltsToPercent[upperIndex]
        val fraction = (percent - lowPercent).toFloat() / (highPercent - lowPercent)
        return lowVolts + fraction * (highVolts - lowVolts)
    }
}
