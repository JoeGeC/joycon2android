package com.joegec.joycon2android.output

class SetOutputModeUseCase(private val repository: OutputRepository) {
    suspend operator fun invoke(mode: OutputMode) = repository.setMode(mode)
}
