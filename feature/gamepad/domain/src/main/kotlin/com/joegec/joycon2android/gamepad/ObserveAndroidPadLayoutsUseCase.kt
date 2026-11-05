package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.ObserveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** Empty unless an Android game is played: an emulator remaps the pad itself, from what Set up wrote. */
class ObserveAndroidPadLayoutsUseCase(private val observeControllerMapping: ObserveControllerMappingUseCase) {
    operator fun invoke(console: Console, bodies: List<PlayerBody>): Flow<Map<PlayerNumber, AndroidPadLayout>> =
        if (console != Console.ANDROID || bodies.isEmpty()) flowOf(emptyMap())
        else combine(bodies.map(::observe)) { it.toMap() }

    private fun observe(body: PlayerBody): Flow<Pair<PlayerNumber, AndroidPadLayout>> =
        observeControllerMapping(Console.ANDROID, body).map { body.player to AndroidPadLayout.of(it, body.side) }
}
