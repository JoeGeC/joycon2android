package com.joegec.joycon2android.mouse

import com.joegec.joycon2android.gamepad.mouse.JoyconMouse
import com.joegec.joycon2android.gamepad.mouse.MouseFrame
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import kotlinx.coroutines.flow.StateFlow

/** Moves the pointer with every Joy-Con on a surface and withholds what it spends from the outputs. */
class MouseRouter(
    private val enabled: StateFlow<Boolean>,
    private val push: (MouseFrame) -> Unit,
) {
    private val mice = mutableMapOf<String, JoyconMouse>()

    fun route(state: AppUiState): AppUiState {
        if (!enabled.value) {
            mice.clear()
            return state
        }
        val joycons = state.unassignedJoycons + state.players.flatMap { listOfNotNull(it.left, it.right) }
        mice.keys.retainAll(joycons.map { it.address }.toSet())
        val frames = joycons.mapNotNull { joycon -> mouseOf(joycon).read(joycon.input)?.let { joycon.address to it } }.toMap()
        push(frames.values.fold(MouseFrame(), MouseFrame::plus))
        if (frames.isEmpty()) return state
        return state.copy(
            players = state.players.map { player ->
                player.copy(left = player.left?.withheldIf(frames), right = player.right?.withheldIf(frames))
            },
        )
    }

    private fun mouseOf(joycon: ConnectedJoycon) = mice.getOrPut(joycon.address) { JoyconMouse() }

    private fun ConnectedJoycon.withheldIf(onSurface: Map<String, MouseFrame>) =
        if (address in onSurface) copy(input = JoyconMouse.withheldFrom(input)) else this
}
