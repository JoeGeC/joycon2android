package com.joegec.joycon2android.emulator

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** Only an output Eden reads gets the choice, so Dolphin keeps its sideways bindings. */
class EdenSingleJoycons(private val bodies: StateFlow<Set<PlayerBody>>) {

    fun markedFor(console: Console, state: PlayerState): PlayerState =
        if (console == Console.SWITCH_PRO && state.body() in bodies.value) state.copy(asSingleJoycon = true)
        else state

    fun marked(states: List<PlayerState>): List<PlayerState> = states.map { markedFor(Console.SWITCH_PRO, it) }
}
