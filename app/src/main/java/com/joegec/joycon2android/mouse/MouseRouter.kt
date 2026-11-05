package com.joegec.joycon2android.mouse

import com.joegec.joycon2android.gamepad.mouse.JoyconMouse
import com.joegec.joycon2android.gamepad.mouse.MouseFrame
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.StateFlow

/**
 * Moves the pointer with every Joy-Con on a surface, but those of a player whose mouse is off. One no
 * player holds also clicks and scrolls with fixed defaults: docs/virtual-gamepad.md#mouse
 */
class MouseRouter(
    private val enabled: StateFlow<Boolean>,
    private val mouseOff: (PlayerNumber) -> Boolean,
    private val push: (MouseFrame) -> Unit,
) {
    private val mice = mutableMapOf<String, JoyconMouse>()

    fun route(state: AppUiState) {
        if (!enabled.value) {
            mice.clear()
            return
        }
        val owners: List<Pair<ConnectedJoycon, PlayerNumber?>> = state.unassignedJoycons.map { it to null } +
            state.players.filterNot { mouseOff(it.player) }
                .flatMap { player -> listOfNotNull(player.left, player.right).map { it to player.player } }
        mice.keys.retainAll(owners.map { it.first.address }.toSet())
        val frames = owners.mapNotNull { (joycon, owner) -> mouseOf(joycon).read(joycon.input, defaultControls = owner == null) }
        push(frames.fold(MouseFrame(), MouseFrame::plus))
    }

    private fun mouseOf(joycon: ConnectedJoycon) = mice.getOrPut(joycon.address) { JoyconMouse(joycon.side) }
}
