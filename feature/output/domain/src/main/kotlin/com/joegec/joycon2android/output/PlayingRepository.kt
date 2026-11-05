package com.joegec.joycon2android.output

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface PlayingRepository {
    val selection: Flow<PlayingSelection>

    /** Not kept across launches: the card starts off, and [startRemembered] brings it back if it was left on. */
    val on: StateFlow<Boolean>

    suspend fun setPlaying(playing: Playing)
    suspend fun selectEmulator(playing: Playing, id: String)
    suspend fun setMotion(on: Boolean)

    /** Also remembered, so the card switched off stays off when players next arrive. */
    suspend fun setOn(on: Boolean)
    suspend fun startRemembered()

    /** Leaves what is remembered, so the card comes back on with the next players. */
    fun stopAll()
}
