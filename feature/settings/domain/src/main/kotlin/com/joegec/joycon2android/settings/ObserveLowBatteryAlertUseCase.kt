package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

class ObserveLowBatteryAlertUseCase(private val repository: LowBatteryAlertRepository) {
    operator fun invoke(): Flow<LowBatteryAlert> = repository.alert
}
