package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ObserveGlobalMappingUseCase(
    private val observePlayerMapping: ObservePlayerMappingUseCase,
    private val globalLayouts: GlobalLayoutRepository,
) {
    operator fun invoke(console: Console, bodies: List<PlayerBody>): Flow<GlobalMapping> =
        combine(everyPlayer(console, bodies), forConsole(console)) { players, saved ->
            GlobalMapping(players, saved)
        }

    private fun everyPlayer(console: Console, bodies: List<PlayerBody>): Flow<List<PlayerMapping>> =
        if (bodies.isEmpty()) flowOf(emptyList())
        else combine(bodies.map { observePlayerMapping(console, it) }) { it.toList() }

    private fun forConsole(console: Console) =
        globalLayouts.observe().map { layouts -> layouts.filter { it.console == console } }
}
