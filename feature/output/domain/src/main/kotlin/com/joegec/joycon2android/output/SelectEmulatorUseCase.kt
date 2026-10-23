package com.joegec.joycon2android.output

class SelectEmulatorUseCase(private val repository: OutputRepository) {
    suspend operator fun invoke(id: String) = repository.selectEmulator(id)
}
