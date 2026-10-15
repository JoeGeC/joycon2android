package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** The lone bodies Eden is set up to see as a Joy-Con rather than a Pro Controller. */
class ObserveSingleJoyconBodiesUseCase(private val observeControllerMapping: ObserveControllerMappingUseCase) {

    operator fun invoke(): Flow<Set<PlayerBody>> =
        combine(LONE_BODIES.map { body -> observeControllerMapping(Console.SWITCH_PRO, body).map { body to it } }) {
            it.filter { (body, mapping) -> mapping.isSingleJoycon(Console.SWITCH_PRO, body.side) }
                .mapTo(mutableSetOf()) { (body, _) -> body }
        }

    companion object {
        val LONE_BODIES: List<PlayerBody> = PlayerNumber.entries.flatMap { player ->
            JoyconSide.entries.filter { it.isLone }.map { PlayerBody(player, it) }
        }
    }
}
