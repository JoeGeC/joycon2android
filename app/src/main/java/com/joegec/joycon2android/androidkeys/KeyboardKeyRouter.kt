package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.model.KeyboardKey
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState

/** Holds each output's bound keyboard keys and withholds their sources from that output's game. */
class KeyboardKeyRouter(
    private val gamepad: OutputBindings<KeyboardKey>,
    private val dsu: OutputBindings<KeyboardKey>,
) {
    private var heldByGamepad = emptyMap<PlayerNumber, Set<KeyboardKey>>()
    private var heldByDsu = emptyMap<PlayerNumber, Set<KeyboardKey>>()

    fun route(routed: RoutedOutputs): RoutedOutputs {
        heldByGamepad = held(gamepad, routed.gamepad.map { it.state }, heldByGamepad)
        heldByDsu = held(dsu, routed.dsu, heldByDsu)
        return routed.copy(
            gamepad = routed.gamepad.map { it.copy(state = gamepad.of(it.state).withheldFrom(it.state)) },
            dsu = routed.dsu.map { dsu.of(it).withheldFrom(it) },
            keyboard = routed.keyboard.copy(keys = (heldByGamepad.values + heldByDsu.values).flatten().toSet()),
        )
    }

    private fun held(
        output: OutputBindings<KeyboardKey>,
        players: List<PlayerState>,
        wasHeld: Map<PlayerNumber, Set<KeyboardKey>>,
    ) = players.associate { it.player to output.heldKeys(it, wasHeld[it.player].orEmpty()) }
}
