package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerState

/** Sources that act only held together, in the hardware's own axes like [KeyBindings]. */
data class HoldCombo(val sources: List<MappingSource>) {

    fun heldBy(state: PlayerState): Boolean = sources.isNotEmpty() && sources.all { it.holds(state) }

    private fun MappingSource.holds(state: PlayerState): Boolean = when (this) {
        is MappingSource.Button -> button.id in state.pressed
        is MappingSource.Stick -> state.tilt(stick, direction) >= PRESS_TILT
    }

    private companion object {
        const val PRESS_TILT = 0.5f
    }
}
