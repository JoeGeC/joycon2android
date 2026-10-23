package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.buttonmapping.KeyBindings
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** One output's key bindings, from the mapping of the console its card is set to. */
class OutputBindings<K>(
    private val enabled: StateFlow<Boolean>,
    private val bindings: StateFlow<Map<PlayerNumber, KeyBindings<K>>>,
) {
    val isEnabled: Boolean get() = enabled.value

    fun of(player: PlayerState): KeyBindings<K> = bindings.value[player.player] ?: KeyBindings()

    fun bindsAny(player: PlayerNumber): Boolean = isEnabled && bindings.value[player]?.sources.orEmpty().isNotEmpty()

    fun heldKeys(player: PlayerState, wasHeld: Set<K> = emptySet()): Set<K> =
        if (isEnabled) of(player).heldKeys(player, wasHeld) else emptySet()
}
