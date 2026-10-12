package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.AndroidKeyBindings
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ObserveAndroidKeyBindingsUseCase(private val observeControllerMapping: ObserveControllerMappingUseCase) {
    operator fun invoke(console: Console, bodies: List<PlayerBody>): Flow<Map<PlayerNumber, AndroidKeyBindings>> =
        if (bodies.isEmpty()) flowOf(emptyMap())
        else combine(bodies.map { body -> observeControllerMapping(console, body).map { body.player to it.androidKeyBindings() } }) {
            it.toMap()
        }
}
