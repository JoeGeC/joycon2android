package com.joegec.joycon2android.settings

data class LowBatteryAlert(
    val notify: Boolean = true,
    val vibrate: Boolean = true,
    val firstWarningPercent: Int = DEFAULT_WARNING_PERCENT,
    val finalWarningPercent: Int = DEFAULT_WARNING_PERCENT,
) {
    val warningPercents: Set<Int> get() = setOf(firstWarningPercent, finalWarningPercent)
    val anyWarning: Boolean get() = notify || vibrate

    companion object {
        const val DEFAULT_WARNING_PERCENT = 10
        const val MIN_WARNING_PERCENT = 5
        const val MAX_WARNING_PERCENT = 50
        const val WARNING_STEP_PERCENT = 5
    }
}
