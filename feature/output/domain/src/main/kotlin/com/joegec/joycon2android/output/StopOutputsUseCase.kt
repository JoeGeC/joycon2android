package com.joegec.joycon2android.output

class StopOutputsUseCase(private val repository: PlayingRepository) {
    operator fun invoke() = repository.stopAll()
}
