package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.PlayerNumber

class OnPlayerAssignedUseCase(private val repository: GamepadRepository) {
    operator fun invoke(player: PlayerNumber) = repository.onPlayerAssigned(player)
}
