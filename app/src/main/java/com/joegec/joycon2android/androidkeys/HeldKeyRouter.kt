package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState

/**
 * Holds each output's bound keys, read from [AppUiState] before any key is withheld so one button
 * can fire several kinds, and withholds their sources from that output's game.
 */
class HeldKeyRouter<K>(
    private val gamepad: OutputBindings<K>,
    private val dsu: OutputBindings<K>,
    private val deliver: RoutedOutputs.(held: Set<K>) -> RoutedOutputs,
) {
    private var heldByGamepad = emptyMap<PlayerNumber, Set<K>>()
    private var heldByDsu = emptyMap<PlayerNumber, Set<K>>()

    fun route(state: AppUiState, routed: RoutedOutputs): RoutedOutputs {
        heldByGamepad = held(gamepad, state.players, heldByGamepad)
        heldByDsu = held(dsu, state.activePlayers, heldByDsu)
        return routed.copy(
            gamepad = routed.gamepad.map { it.copy(state = gamepad.of(it.state).withheldFrom(it.state)) },
            dsu = routed.dsu.map { dsu.of(it).withheldFrom(it) },
        ).deliver((heldByGamepad.values + heldByDsu.values).flatten().toSet())
    }

    private fun held(output: OutputBindings<K>, players: List<PlayerState>, wasHeld: Map<PlayerNumber, Set<K>>) =
        players.associate { it.player to output.heldKeys(it, wasHeld[it.player].orEmpty()) }
}
