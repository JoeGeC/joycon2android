package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber

/**
 * Holds each player's bound keys, read from [AppUiState] before any key is withheld so one button
 * can fire several kinds, and withholds their sources from both outputs.
 */
class HeldKeyRouter<K>(
    private val bindings: OutputBindings<K>,
    private val deliver: RoutedOutputs.(held: Set<K>) -> RoutedOutputs,
) {
    private var held = emptyMap<PlayerNumber, Set<K>>()

    fun route(state: AppUiState, routed: RoutedOutputs): RoutedOutputs {
        held = state.players.associate { it.player to bindings.heldKeys(it, held[it.player].orEmpty()) }
        return routed.copy(
            gamepad = routed.gamepad.map { it.copy(state = bindings.of(it.state).withheldFrom(it.state)) },
            dsu = routed.dsu.map { bindings.of(it).withheldFrom(it) },
        ).deliver(held.values.flatten().toSet())
    }
}
