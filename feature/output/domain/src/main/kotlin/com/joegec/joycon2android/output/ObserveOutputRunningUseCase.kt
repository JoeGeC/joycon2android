package com.joegec.joycon2android.output

class ObserveOutputRunningUseCase(private val repository: OutputRepository) {
    operator fun invoke() = repository.running
}
