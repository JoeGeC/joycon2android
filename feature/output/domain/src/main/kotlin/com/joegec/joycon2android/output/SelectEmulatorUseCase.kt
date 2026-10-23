package com.joegec.joycon2android.output

class SelectEmulatorUseCase(private val repository: OutputRepository) {
    suspend operator fun invoke(mode: OutputMode, id: String) = repository.selectEmulator(mode, id)
}
