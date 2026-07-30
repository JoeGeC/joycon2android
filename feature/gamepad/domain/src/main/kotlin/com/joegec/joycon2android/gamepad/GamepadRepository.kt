package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** The system-wide virtual gamepad output, as the domain sees it. */
interface GamepadRepository {
    val enabled: StateFlow<Boolean>
    val error: StateFlow<String?>

    fun enable(players: List<PlayerState>)
    fun disable()
    fun push(players: List<PlayerState>)
    fun onPlayerAssigned(player: PlayerNumber)
    fun onPlayerUnassigned(player: PlayerNumber)
}
