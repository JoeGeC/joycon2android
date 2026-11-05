package com.joegec.joycon2android.output

class SelectEmulatorUseCase(private val repository: PlayingRepository) {
    suspend operator fun invoke(playing: Playing, id: String) = repository.selectEmulator(playing, id)
}
