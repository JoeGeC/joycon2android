package com.joegec.joycon2android.gamepad.mouse

class SetMouseEnabledUseCase(private val repository: MouseRepository) {
    operator fun invoke(enabled: Boolean) = if (enabled) repository.enable() else repository.disable()
}
