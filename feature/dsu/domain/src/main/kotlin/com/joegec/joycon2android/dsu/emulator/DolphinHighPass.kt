package com.joegec.joycon2android.dsu.emulator

/** Not Dolphin's smooth(), which breaks on newer phones: docs/dsu-motion.md#high-pass-without-smooth */
internal object DolphinHighPass {
    // A slew limit no tilt outruns but a swing does, so subtracting it leaves the motion and drops gravity.
    private const val TILT_SETTLE_SECONDS = 0.03
    private const val CLOCK_PERIOD_SECONDS = 1000

    // Caps the first step, a pause, and the step where the clock wraps.
    private const val MAX_STEP_SECONDS = 0.1

    /** [signed] minus a slew-limited copy of it, as one Dolphin expression. */
    fun of(signed: String, control: String): String {
        val prefix = "$" + control.replace('/', '_')
        val now = "${prefix}_now"
        val last = "${prefix}_last"
        val step = "${prefix}_step"
        val slew = "${prefix}_slew"
        return listOf(
            "$now = timer($CLOCK_PERIOD_SECONDS)",
            "$step = clamp(($now - $last) * $CLOCK_PERIOD_SECONDS, 0, $MAX_STEP_SECONDS) / $TILT_SETTLE_SECONDS",
            "$last = $now",
            "$slew = $slew + clamp($signed - $slew, 0 - $step, $step)",
            "$signed - $slew",
        ).joinToString(", ", prefix = "(", postfix = ")")
    }
}
