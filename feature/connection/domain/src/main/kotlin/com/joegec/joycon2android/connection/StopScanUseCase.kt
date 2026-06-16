package com.joegec.joycon2android.connection

class StopScanUseCase(private val repository: ControllerRepository) {
    operator fun invoke() = repository.stopScan()
}
