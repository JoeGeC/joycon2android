package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** The DSU motion server, as the domain sees it. Implemented in the data layer. */
interface DsuRepository {
    val enabled: StateFlow<Boolean>
    val error: StateFlow<String?>
    val clientCount: StateFlow<Int>
    val lanEnabled: StateFlow<Boolean>

    /** Address an emulator should connect to (e.g. `127.0.0.1:26760`), or null when off. */
    val address: StateFlow<String?>

    fun enable()
    fun disable()
    fun setLanEnabled(enabled: Boolean)
    fun push(players: List<PlayerState>)
}
