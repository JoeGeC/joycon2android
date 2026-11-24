package com.joegec.joycon2android.settings

class SetLowBatteryVibrationUseCase(private val repository: LowBatteryAlertRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setVibrate(enabled)
}
