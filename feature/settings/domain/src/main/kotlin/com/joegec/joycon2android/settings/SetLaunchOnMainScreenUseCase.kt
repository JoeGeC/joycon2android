package com.joegec.joycon2android.settings

class SetLaunchOnMainScreenUseCase(private val repository: MainScreenLaunchRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setLaunchOnMainScreen(enabled)
}
