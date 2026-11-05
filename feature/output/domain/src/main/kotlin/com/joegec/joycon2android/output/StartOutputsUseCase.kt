package com.joegec.joycon2android.output

class StartOutputsUseCase(private val repository: PlayingRepository) {
    operator fun invoke() = repository.start()
}
