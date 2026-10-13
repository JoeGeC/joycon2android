package com.joegec.joycon2android.session

import com.joegec.joycon2android.model.ControllerModel

class SetControllerTypeUseCase(private val coordinator: SessionCoordinator) {
    operator fun invoke(address: String, model: ControllerModel?) = coordinator.setControllerType(address, model)
}
