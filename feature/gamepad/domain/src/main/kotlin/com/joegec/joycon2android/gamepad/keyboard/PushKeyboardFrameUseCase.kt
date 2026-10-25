package com.joegec.joycon2android.gamepad.keyboard

class PushKeyboardFrameUseCase(private val repository: KeyboardRepository) {
    operator fun invoke(frame: KeyboardFrame) = repository.push(frame)
}
