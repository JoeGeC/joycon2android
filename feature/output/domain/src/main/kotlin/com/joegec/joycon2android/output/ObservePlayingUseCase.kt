package com.joegec.joycon2android.output

class ObservePlayingUseCase(private val repository: PlayingRepository) {
    operator fun invoke() = repository.selection
}
