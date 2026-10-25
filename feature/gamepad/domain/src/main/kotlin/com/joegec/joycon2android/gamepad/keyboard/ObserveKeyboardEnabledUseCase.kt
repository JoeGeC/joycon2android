package com.joegec.joycon2android.gamepad.keyboard

class ObserveKeyboardEnabledUseCase(private val repository: KeyboardRepository) {
    operator fun invoke() = repository.enabled
}
