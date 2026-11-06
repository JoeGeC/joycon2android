package com.joegec.joycon2android.output

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PlayingRepository {
    val selection: Flow<PlayingSelection>

    /** On from the first player until [stopAll]. */
    val on: StateFlow<Boolean>

    suspend fun setPlaying(playing: Playing)
    suspend fun selectEmulator(playing: Playing, id: String)
    suspend fun setMotion(on: Boolean)
    suspend fun setPad(on: Boolean)
    fun start()
    fun stopAll()
}
