package com.joegec.joycon2android.output

class SetOutputRunningUseCase(private val repository: OutputRepository) {
    suspend operator fun invoke(mode: OutputMode, running: Boolean) = repository.setRunning(mode, running)
}
