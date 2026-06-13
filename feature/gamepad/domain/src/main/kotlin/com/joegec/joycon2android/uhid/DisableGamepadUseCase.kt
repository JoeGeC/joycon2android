package com.joegec.joycon2android.uhid

class DisableGamepadUseCase(private val repository: GamepadRepository) {
    operator fun invoke() = repository.disable()
}
