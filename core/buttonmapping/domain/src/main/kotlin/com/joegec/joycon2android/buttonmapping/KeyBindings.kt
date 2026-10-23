package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerState

/**
 * Keys the app presses itself. Buttons as the controller reports them and stick directions in the
 * hardware's own axes, both before any sideways rotation.
 */
data class KeyBindings<K>(val sources: Map<K, List<MappingSource>> = emptyMap()) {

    private val boundButtonIds: Set<String> =
        sources.values.flatten().filterIsInstance<MappingSource.Button>().mapTo(mutableSetOf()) { it.button.id }
    private val boundTilts: List<MappingSource.Stick> =
        sources.values.flatten().filterIsInstance<MappingSource.Stick>().distinct()

    /** [wasHeld] lets a held stick key ride a little below the tilt that pressed it, so it never chatters. */
    fun heldKeys(state: PlayerState, wasHeld: Set<K> = emptySet()): Set<K> =
        sources.filter { (key, bound) -> bound.any { it.holds(state, key in wasHeld) } }.keys

    fun withheldFrom(state: PlayerState): PlayerState {
        if (boundButtonIds.isEmpty() && boundTilts.isEmpty()) return state
        val buttonsWithheld = state.copy(
            left = state.left?.let { it.copy(input = it.input.copy(pressed = it.input.pressed - boundButtonIds)) },
            right = state.right?.let { it.copy(input = it.input.copy(pressed = it.input.pressed - boundButtonIds)) },
        )
        return boundTilts.fold(buttonsWithheld) { acc, tilt -> acc.withoutTilt(tilt.stick, tilt.direction) }
    }

    private fun MappingSource.holds(state: PlayerState, held: Boolean): Boolean = when (this) {
        is MappingSource.Button -> button.id in state.pressed
        is MappingSource.Stick -> state.tilt(stick, direction) >= if (held) RELEASE_TILT else PRESS_TILT
    }

    private companion object {
        const val PRESS_TILT = 0.5f
        const val RELEASE_TILT = 0.35f
    }
}
