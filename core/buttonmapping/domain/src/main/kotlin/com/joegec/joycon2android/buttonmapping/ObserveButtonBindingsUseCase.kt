package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.ButtonBindings
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ObserveButtonBindingsUseCase<K>(
    private val observeControllerMapping: ObserveControllerMappingUseCase,
    private val bindingsOf: (Map<String, String>) -> ButtonBindings<K>,
) {
    operator fun invoke(console: Console, bodies: List<PlayerBody>): Flow<Map<PlayerNumber, ButtonBindings<K>>> =
        if (bodies.isEmpty()) flowOf(emptyMap())
        else combine(bodies.map { body -> observeControllerMapping(console, body).map { body.player to bindingsOf(it) } }) {
            it.toMap()
        }
}
