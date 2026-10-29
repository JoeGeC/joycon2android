package com.joegec.joycon2android

import com.joegec.joycon2android.buttonmapping.DEFAULT_RUMBLE_PERCENT
import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Rumble

/** Plays a player's virtual-gamepad rumble on the controllers in their hands, at their rumble strength. */
class ControllerRumble(
    private val controllers: ControllerRepository,
    private val players: () -> List<PlayerState>,
    private val strengths: () -> Map<PlayerNumber, Int>,
) {

    fun play(player: PlayerNumber, rumble: Rumble) {
        val state = players().find { it.player == player } ?: return
        val percent = strengths()[player] ?: DEFAULT_RUMBLE_PERCENT
        val scaled = rumble.scaledBy(percent / 100f)
        listOfNotNull(state.left, state.right).map { it.address }.distinct().forEach { controllers.rumble(it, scaled) }
    }
}
