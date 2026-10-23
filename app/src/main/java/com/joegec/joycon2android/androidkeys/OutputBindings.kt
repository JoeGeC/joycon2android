package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.model.ButtonBindings
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** One output's key bindings, from the mapping of the console its card is set to. */
class OutputBindings<K>(
    private val enabled: StateFlow<Boolean>,
    private val bindings: StateFlow<Map<PlayerNumber, ButtonBindings<K>>>,
) {
    val isEnabled: Boolean get() = enabled.value

    fun of(player: PlayerState): ButtonBindings<K> = bindings.value[player.player] ?: ButtonBindings()

    fun heldKeys(player: PlayerState): Set<K> = if (isEnabled) of(player).heldKeys(player) else emptySet()
}
