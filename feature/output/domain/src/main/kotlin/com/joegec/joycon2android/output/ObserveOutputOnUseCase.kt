package com.joegec.joycon2android.output

class ObserveOutputOnUseCase(private val repository: PlayingRepository) {
    operator fun invoke() = repository.on
}
