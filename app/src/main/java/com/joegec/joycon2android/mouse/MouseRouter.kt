package com.joegec.joycon2android.mouse

import com.joegec.joycon2android.gamepad.mouse.JoyconMouse
import com.joegec.joycon2android.gamepad.mouse.MouseFrame
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.StateFlow

/**
 * Moves the pointer with every Joy-Con on a surface. One whose player maps no mouse action also
 * clicks and scrolls with the defaults, withheld from the outputs: docs/virtual-gamepad.md#mouse
 */
class MouseRouter(
    private val enabled: StateFlow<Boolean>,
    private val mapsMouse: (PlayerNumber) -> Boolean,
    private val push: (MouseFrame) -> Unit,
) {
    private val mice = mutableMapOf<String, JoyconMouse>()

    fun route(state: AppUiState): AppUiState {
        if (!enabled.value) {
            mice.clear()
            return state
        }
        val owners: List<Pair<ConnectedJoycon, PlayerNumber?>> = state.unassignedJoycons.map { it to null } +
            state.players.flatMap { player -> listOfNotNull(player.left, player.right).map { it to player.player } }
        mice.keys.retainAll(owners.map { it.first.address }.toSet())
        val defaulted = mutableSetOf<String>()
        val frames = owners.mapNotNull { (joycon, owner) ->
            val defaults = owner == null || !mapsMouse(owner)
            mouseOf(joycon).read(joycon.input, defaults)?.also { if (defaults) defaulted += joycon.address }
        }
        push(frames.fold(MouseFrame(), MouseFrame::plus))
        if (defaulted.isEmpty()) return state
        return state.copy(
            players = state.players.map { player ->
                player.copy(left = player.left?.withheldIf(defaulted), right = player.right?.withheldIf(defaulted))
            },
        )
    }

    private fun mouseOf(joycon: ConnectedJoycon) = mice.getOrPut(joycon.address) { JoyconMouse(joycon.side) }

    private fun ConnectedJoycon.withheldIf(defaulted: Set<String>) =
        if (address in defaulted) copy(input = JoyconMouse.withheldFrom(input)) else this
}
