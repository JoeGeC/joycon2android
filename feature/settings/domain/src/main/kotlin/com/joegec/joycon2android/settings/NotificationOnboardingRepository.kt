package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

interface NotificationOnboardingRepository {
    val seen: Flow<Boolean>
    suspend fun markSeen()
}
