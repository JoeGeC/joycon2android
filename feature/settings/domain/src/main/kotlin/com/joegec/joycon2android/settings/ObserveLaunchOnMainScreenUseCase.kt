package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

class ObserveLaunchOnMainScreenUseCase(private val repository: MainScreenLaunchRepository) {
    operator fun invoke(): Flow<Boolean> = repository.launchOnMainScreen
}
