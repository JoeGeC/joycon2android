package com.joegec.joycon2android.ble

class DisconnectControllerUseCase(private val repository: ControllerRepository) {
    operator fun invoke(address: String) = repository.disconnect(address)
}
