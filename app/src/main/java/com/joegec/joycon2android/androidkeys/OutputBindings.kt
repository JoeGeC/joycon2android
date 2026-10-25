package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.buttonmapping.KeyBindings
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** The active console's key bindings, while [acting]: the output runs and they aren't paused. */
class OutputBindings<K>(
    private val acting: StateFlow<Boolean>,
    private val bindings: StateFlow<Map<PlayerNumber, KeyBindings<K>>>,
) {
    fun of(player: PlayerState): KeyBindings<K> = of(player.player)

    fun heldKeys(player: PlayerState, wasHeld: Set<K> = emptySet()): Set<K> = of(player).heldKeys(player, wasHeld)

    fun bindsAny(player: PlayerNumber): Boolean = of(player).sources.isNotEmpty()

    private fun of(player: PlayerNumber): KeyBindings<K> =
        if (acting.value) bindings.value[player] ?: KeyBindings() else KeyBindings()
}
