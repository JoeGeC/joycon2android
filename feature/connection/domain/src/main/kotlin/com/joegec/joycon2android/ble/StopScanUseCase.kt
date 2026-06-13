package com.joegec.joycon2android.ble

class StopScanUseCase(private val repository: ControllerRepository) {
    operator fun invoke() = repository.stopScan()
}
