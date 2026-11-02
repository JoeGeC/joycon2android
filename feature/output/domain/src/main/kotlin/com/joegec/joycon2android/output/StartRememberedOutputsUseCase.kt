package com.joegec.joycon2android.output

class StartRememberedOutputsUseCase(private val repository: OutputRepository) {
    suspend operator fun invoke() = repository.startRemembered()
}
