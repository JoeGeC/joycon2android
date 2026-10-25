package com.joegec.joycon2android.gamepad.mouse

class ObserveMouseEnabledUseCase(private val repository: MouseRepository) {
    operator fun invoke() = repository.enabled
}
