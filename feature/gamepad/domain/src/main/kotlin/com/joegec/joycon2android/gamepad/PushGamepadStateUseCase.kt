package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.PlayerState

class PushGamepadStateUseCase(private val repository: GamepadRepository) {
    operator fun invoke(players: List<PlayerState>) = repository.push(players)
}
