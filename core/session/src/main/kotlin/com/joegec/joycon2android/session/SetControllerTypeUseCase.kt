package com.joegec.joycon2android.session

import com.joegec.joycon2android.model.Side

class SetControllerTypeUseCase(private val coordinator: SessionCoordinator) {
    operator fun invoke(address: String, side: Side?) = coordinator.setControllerType(address, side)
}
