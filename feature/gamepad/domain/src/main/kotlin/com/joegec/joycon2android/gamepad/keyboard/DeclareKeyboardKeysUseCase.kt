package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.model.KeyboardKey

class DeclareKeyboardKeysUseCase(private val repository: KeyboardRepository) {
    operator fun invoke(bound: Set<KeyboardKey>) = repository.declare(bound)
}
