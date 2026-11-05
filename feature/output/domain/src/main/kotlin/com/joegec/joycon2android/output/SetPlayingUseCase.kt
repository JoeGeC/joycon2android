package com.joegec.joycon2android.output

class SetPlayingUseCase(private val repository: PlayingRepository) {
    suspend operator fun invoke(playing: Playing) = repository.setPlaying(playing)
}
