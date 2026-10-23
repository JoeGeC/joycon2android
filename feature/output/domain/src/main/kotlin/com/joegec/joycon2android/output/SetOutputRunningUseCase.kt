package com.joegec.joycon2android.output

class SetOutputRunningUseCase(private val repository: OutputRepository) {
    operator fun invoke(running: Boolean) = repository.setRunning(running)
}
