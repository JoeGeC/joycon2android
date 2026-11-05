package com.joegec.joycon2android.output

class SetMotionUseCase(private val repository: PlayingRepository) {
    suspend operator fun invoke(on: Boolean) = repository.setMotion(on)
}
