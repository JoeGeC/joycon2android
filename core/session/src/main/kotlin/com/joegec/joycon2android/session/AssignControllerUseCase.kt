package com.joegec.joycon2android.session

import com.joegec.joycon2android.model.PlayerNumber

class AssignControllerUseCase(private val coordinator: SessionCoordinator) {
    operator fun invoke(address: String, player: PlayerNumber) = coordinator.assign(address, player)
}
