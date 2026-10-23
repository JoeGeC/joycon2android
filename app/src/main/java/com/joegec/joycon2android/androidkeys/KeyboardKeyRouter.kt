package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.model.KeyboardKey

/** Holds each output's bound keyboard keys and withholds their buttons from that output's game. */
class KeyboardKeyRouter(
    private val gamepad: OutputBindings<KeyboardKey>,
    private val dsu: OutputBindings<KeyboardKey>,
) {

    fun route(routed: RoutedOutputs): RoutedOutputs {
        val held = routed.gamepad.flatMap { gamepad.heldKeys(it.state) } + routed.dsu.flatMap(dsu::heldKeys)
        return routed.copy(
            gamepad = routed.gamepad.map { it.copy(state = gamepad.of(it.state).withheldFrom(it.state)) },
            dsu = routed.dsu.map { dsu.of(it).withheldFrom(it) },
            keyboard = routed.keyboard.copy(keys = held.toSet()),
        )
    }
}
