package com.joegec.joycon2android.dsu

class DisableDsuUseCase(private val repository: DsuRepository) {
    operator fun invoke() = repository.disable()
}
