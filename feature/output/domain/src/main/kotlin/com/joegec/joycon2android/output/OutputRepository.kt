package com.joegec.joycon2android.output

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface OutputRepository {
    val selection: Flow<OutputSelection>

    /** Not kept across launches: the outputs start off, as they always have. */
    val running: StateFlow<Boolean>

    suspend fun setMode(mode: OutputMode)
    suspend fun selectEmulator(id: String)
    suspend fun setOverride(device: OutputDevice, override: DeviceOverride)
    fun setRunning(running: Boolean)
}
