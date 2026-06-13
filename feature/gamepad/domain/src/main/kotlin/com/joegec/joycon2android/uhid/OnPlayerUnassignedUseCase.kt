package com.joegec.joycon2android.uhid

import com.joegec.joycon2android.model.PlayerNumber

class OnPlayerUnassignedUseCase(private val repository: GamepadRepository) {
    operator fun invoke(player: PlayerNumber) = repository.onPlayerUnassigned(player)
}
