package com.joegec.joycon2android.connection

import kotlin.math.hypot
import kotlin.math.roundToInt

/** Why the outer ring counts as full tilt: docs/protocol.md#stick-range-and-centre */
internal object StickRim {
    private const val CENTER = 2048
    private const val MAX = 4095

    // Fraction of the calibrated radius.
    private const val FULL_TILT_RADIUS = 0.9f

    fun saturate(x: Int, y: Int): Pair<Int, Int> {
        val dx = (x - CENTER).toFloat() / CENTER
        val dy = (y - CENTER).toFloat() / CENTER
        val radius = hypot(dx, dy)
        if (radius == 0f) return x to y
        val scale = minOf(radius / FULL_TILT_RADIUS, 1f) / radius
        return axis(dx * scale) to axis(dy * scale)
    }

    private fun axis(lean: Float): Int = (CENTER + lean * CENTER).roundToInt().coerceIn(0, MAX)
}
