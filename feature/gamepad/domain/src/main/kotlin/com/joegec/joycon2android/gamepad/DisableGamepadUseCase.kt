package com.joegec.joycon2android.gamepad

class DisableGamepadUseCase(private val repository: GamepadRepository) {
    operator fun invoke() = repository.disable()
}
