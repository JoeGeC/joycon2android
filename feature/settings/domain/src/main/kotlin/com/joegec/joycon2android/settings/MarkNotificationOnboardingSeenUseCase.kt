package com.joegec.joycon2android.settings

class MarkNotificationOnboardingSeenUseCase(private val repository: NotificationOnboardingRepository) {
    suspend operator fun invoke() = repository.markSeen()
}
