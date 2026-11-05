package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/** The players whose mapping on any of the consoles uses the Joy-Con mouse. */
class ObserveJoyconMouseUsersUseCase(private val observeControllerMapping: ObserveControllerMappingUseCase) {
    operator fun invoke(consoles: Set<Console>, bodies: List<PlayerBody>): Flow<Set<PlayerNumber>> {
        val mappings = consoles.flatMap { console -> bodies.map { console to it } }
        if (mappings.isEmpty()) return flowOf(emptySet())
        return combine(
            mappings.map { (console, body) ->
                observeControllerMapping(console, body).map { entries -> body.player.takeIf { entries.joyconMouse(console) } }
            },
        ) { users -> users.filterNotNull().toSet() }
    }
}
