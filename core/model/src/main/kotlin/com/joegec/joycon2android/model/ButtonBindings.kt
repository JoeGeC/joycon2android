package com.joegec.joycon2android.model

/** Keys the app presses itself, bound by the buttons the controller reports, before any sideways rotation. */
data class ButtonBindings<K>(val buttons: Map<K, Set<JoyconButton>> = emptyMap()) {

    private val boundIds: Set<String> = buttons.values.flatten().mapTo(mutableSetOf()) { it.id }

    fun heldKeys(state: PlayerState): Set<K> =
        buttons.filterValues { bound -> bound.any { it.id in state.pressed } }.keys

    fun withheldFrom(state: PlayerState): PlayerState {
        if (boundIds.isEmpty()) return state
        return state.copy(left = state.left?.withoutBound(), right = state.right?.withoutBound())
    }

    private fun ConnectedJoycon.withoutBound() = copy(input = input.copy(pressed = input.pressed - boundIds))
}
