package com.joegec.joycon2android.uhid

import com.joegec.joycon2android.model.PlayerState

class EnableGamepadUseCase(private val repository: GamepadRepository) {
    operator fun invoke(players: List<PlayerState>) = repository.enable(players)
}
