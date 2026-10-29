package com.joegec.joycon2android

import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Rumble

/** Plays a player's virtual-gamepad rumble on the controllers in their hands. */
class ControllerRumble(
    private val controllers: ControllerRepository,
    private val players: () -> List<PlayerState>,
) {

    fun play(player: PlayerNumber, rumble: Rumble) {
        val state = players().find { it.player == player } ?: return
        listOfNotNull(state.left, state.right).map { it.address }.distinct().forEach { controllers.rumble(it, rumble) }
    }
}
