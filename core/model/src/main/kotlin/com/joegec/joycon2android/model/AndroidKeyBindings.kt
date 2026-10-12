package com.joegec.joycon2android.model

/** Bound by the buttons the controller reports, before any sideways rotation. */
data class AndroidKeyBindings(val buttons: Map<AndroidKey, Set<JoyconButton>> = emptyMap()) {

    private val boundIds: Set<String> = buttons.values.flatten().mapTo(mutableSetOf()) { it.id }

    fun heldKeys(state: PlayerState): Set<AndroidKey> =
        buttons.filterValues { bound -> bound.any { it.id in state.pressed } }.keys

    fun withheldFrom(state: PlayerState): PlayerState {
        if (boundIds.isEmpty()) return state
        return state.copy(left = state.left?.withoutBound(), right = state.right?.withoutBound())
    }

    private fun ConnectedJoycon.withoutBound() = copy(input = input.copy(pressed = input.pressed - boundIds))
}
