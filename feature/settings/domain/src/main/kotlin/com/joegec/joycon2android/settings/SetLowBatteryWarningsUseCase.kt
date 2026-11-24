package com.joegec.joycon2android.settings

class SetLowBatteryWarningsUseCase(private val repository: LowBatteryAlertRepository) {
    suspend operator fun invoke(a: Int, b: Int) {
        val range = LowBatteryAlert.MIN_WARNING_PERCENT..LowBatteryAlert.MAX_WARNING_PERCENT
        repository.setWarningPercents(first = maxOf(a, b).coerceIn(range), final = minOf(a, b).coerceIn(range))
    }
}
