package com.joegec.joycon2android.output

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OutputRepository {
    val selection: Flow<OutputSelection>

    /** Not kept across launches: the output starts off, and on again as players arrive. */
    val running: StateFlow<Boolean>

    suspend fun setMode(mode: OutputMode)
    suspend fun selectEmulator(mode: OutputMode, id: String)
    fun setRunning(running: Boolean)
}
