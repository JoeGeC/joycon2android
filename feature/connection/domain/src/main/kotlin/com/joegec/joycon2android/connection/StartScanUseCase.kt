package com.joegec.joycon2android.connection

class StartScanUseCase(private val repository: ControllerRepository) {
    operator fun invoke() = repository.startScan()
}
