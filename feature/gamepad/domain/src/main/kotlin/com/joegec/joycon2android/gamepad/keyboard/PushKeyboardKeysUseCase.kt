package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.AndroidKey

class PushKeyboardKeysUseCase(private val repository: KeyboardRepository) {
    operator fun invoke(held: Set<AndroidKey>) = repository.push(held)
}
