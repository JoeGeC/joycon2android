package com.joegec.joycon2android.output

class StartRememberedOutputsUseCase(private val repository: PlayingRepository) {
    suspend operator fun invoke() = repository.startRemembered()
}
