package com.joegec.joycon2android.dsu

class EnableDsuUseCase(private val repository: DsuRepository) {
    operator fun invoke() = repository.enable()
}
