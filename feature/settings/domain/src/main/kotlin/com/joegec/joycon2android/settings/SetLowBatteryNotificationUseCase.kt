package com.joegec.joycon2android.settings

class SetLowBatteryNotificationUseCase(private val repository: LowBatteryAlertRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setNotify(enabled)
}
