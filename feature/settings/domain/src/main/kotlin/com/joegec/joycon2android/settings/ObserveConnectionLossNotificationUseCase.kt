package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

class ObserveConnectionLossNotificationUseCase(private val repository: ConnectionLossAlertRepository) {
    operator fun invoke(): Flow<Boolean> = repository.notify
}
