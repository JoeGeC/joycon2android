package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

interface MainScreenLaunchRepository {
    val launchOnMainScreen: Flow<Boolean>
    suspend fun setLaunchOnMainScreen(enabled: Boolean)
}
