package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

class ObserveNotificationOnboardingSeenUseCase(private val repository: NotificationOnboardingRepository) {
    operator fun invoke(): Flow<Boolean> = repository.seen
}
