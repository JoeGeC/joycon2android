package com.joegec.joycon2android.output

class StopOutputsUseCase(private val repository: OutputRepository) {
    operator fun invoke() = repository.stopAll()
}
