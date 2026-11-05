package com.joegec.joycon2android.output

class SetOutputOnUseCase(private val repository: PlayingRepository) {
    suspend operator fun invoke(on: Boolean) = repository.setOn(on)
}
