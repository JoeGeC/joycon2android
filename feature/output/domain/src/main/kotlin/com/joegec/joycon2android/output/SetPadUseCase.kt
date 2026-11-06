package com.joegec.joycon2android.output

class SetPadUseCase(private val repository: PlayingRepository) {
    suspend operator fun invoke(on: Boolean) = repository.setPad(on)
}
