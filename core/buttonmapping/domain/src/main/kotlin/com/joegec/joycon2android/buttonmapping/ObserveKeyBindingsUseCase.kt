package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

class ObserveKeyBindingsUseCase<K>(
    private val observeControllerMapping: ObserveControllerMappingUseCase,
    private val observeWiiController: ObserveWiiControllerUseCase,
    private val bindingsOf: (entries: Map<String, String>, side: JoyconSide, heldSideways: Boolean) -> KeyBindings<K>,
) {
    /** Every console's bindings act together, as when several outputs run at once. */
    operator fun invoke(consoles: Set<Console>, bodies: List<PlayerBody>): Flow<Map<PlayerNumber, KeyBindings<K>>> =
        if (consoles.isEmpty()) flowOf(emptyMap())
        else combine(consoles.map { invoke(it, bodies) }) { perConsole -> perConsole.reduce(::merged) }

    operator fun invoke(console: Console, bodies: List<PlayerBody>): Flow<Map<PlayerNumber, KeyBindings<K>>> =
        if (bodies.isEmpty()) flowOf(emptyMap())
        else combine(bodies.map { observe(console, it) }) { it.toMap() }

    private fun observe(console: Console, body: PlayerBody): Flow<Pair<PlayerNumber, KeyBindings<K>>> =
        combine(observeControllerMapping(console, body), observeWiiController(console, body)) { entries, controller ->
            body.player to bindingsOf(entries, body.side, controller.heldSideways)
        }

    private fun merged(a: Map<PlayerNumber, KeyBindings<K>>, b: Map<PlayerNumber, KeyBindings<K>>) =
        (a.keys + b.keys).associateWith { player -> (a[player] ?: KeyBindings()) + (b[player] ?: KeyBindings()) }
}
