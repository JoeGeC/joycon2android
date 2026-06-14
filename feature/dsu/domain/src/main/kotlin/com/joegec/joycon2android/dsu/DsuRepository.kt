package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

interface DsuRepository {
    val enabled: StateFlow<Boolean>
    val error: StateFlow<String?>
    val clientCount: StateFlow<Int>

    /** Address an emulator should connect to (e.g. `127.0.0.1:26760`), or null when off. */
    val address: StateFlow<String?>

    fun enable()
    fun disable()
    fun push(players: List<PlayerState>)
}
