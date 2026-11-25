package com.joegec.joycon2android.settings

class SetConnectionLossNotificationUseCase(private val repository: ConnectionLossAlertRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setNotify(enabled)
}
