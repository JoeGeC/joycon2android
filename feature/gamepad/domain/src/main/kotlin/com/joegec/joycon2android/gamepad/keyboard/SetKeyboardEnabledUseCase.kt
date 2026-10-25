package com.joegec.joycon2android.gamepad.keyboard

class SetKeyboardEnabledUseCase(private val repository: KeyboardRepository) {
    operator fun invoke(enabled: Boolean) = if (enabled) repository.enable() else repository.disable()
}
