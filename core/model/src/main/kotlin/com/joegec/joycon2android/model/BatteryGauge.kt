package com.joegec.joycon2android.model

import kotlin.math.roundToInt

object BatteryGauge {
    // Share of a full charge's play time left, measured: docs/protocol.md#battery
    private val voltsToPercent = listOf(
        2.390f to 0,
        2.804f to 1,
        2.921f to 2,
        2.997f to 3,
        3.050f to 4,
        3.090f to 5,
        3.116f to 6,
        3.127f to 8,
        3.132f to 10,
        3.135f to 12,
        3.152f to 15,
        3.178f to 20,
        3.196f to 25,
        3.212f to 30,
        3.242f to 40,
        3.279f to 50,
        3.345f to 60,
        3.423f to 70,
        3.503f to 80,
        3.592f to 90,
        3.685f to 100,
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
