package com.joegec.joycon2android.session

class UnassignControllerUseCase(private val coordinator: SessionCoordinator) {
    operator fun invoke(address: String) = coordinator.unassign(address)
}
