package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Everything the editor shows for one player: their bindings, their switch, and what those name. */
class ObservePlayerMappingUseCase(
    private val observeControllerMapping: ObserveControllerMappingUseCase,
    private val observeSidewaysRemote: ObserveSidewaysRemoteUseCase,
    private val savedLayouts: SavedLayoutRepository,
) {
    operator fun invoke(console: Console, body: PlayerBody): Flow<PlayerMapping> =
        combine(
            observeControllerMapping(console, body),
            observeSidewaysRemote(console, body),
            savedLayouts.observe(),
        ) { entries, sidewaysRemote, saved ->
            PlayerMapping(
                body = body,
                entries = entries,
                sidewaysRemote = sidewaysRemote,
                layout = MappingLayouts.matching(console, body.side, entries, sidewaysRemote, saved),
            )
        }
}
