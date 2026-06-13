package com.joegec.joycon2android.ble

class StartScanUseCase(private val repository: ControllerRepository) {
    operator fun invoke() = repository.startScan()
}
