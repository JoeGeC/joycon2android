package com.joegec.joycon2android.output

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OutputRepository {
    val selection: Flow<OutputSelection>

    /** Not kept across launches: every tab starts off, and [startRemembered] brings back the ones left on. */
    val running: StateFlow<Set<OutputMode>>

    suspend fun setMode(mode: OutputMode)
    suspend fun selectEmulator(mode: OutputMode, id: String)

    /** Also remembered, so a tab switched off stays off when players next arrive. */
    suspend fun setRunning(mode: OutputMode, running: Boolean)
    suspend fun startRemembered()

    /** Leaves what is remembered, so the same tabs come back with the next players. */
    fun stopAll()
}
