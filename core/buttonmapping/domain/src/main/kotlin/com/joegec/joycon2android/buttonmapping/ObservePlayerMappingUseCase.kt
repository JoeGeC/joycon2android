package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObservePlayerMappingUseCase(
    private val observeControllerMapping: ObserveControllerMappingUseCase,
    private val observeRemoteMotion: ObserveRemoteMotionUseCase,
    private val savedLayouts: SavedLayoutRepository,
) {
    operator fun invoke(console: Console, body: PlayerBody): Flow<PlayerMapping> =
        combine(
            observeControllerMapping(console, body),
            observeRemoteMotion(console, body),
            savedLayouts.observe(),
        ) { entries, motion, saved ->
            PlayerMapping(
                body = body,
                entries = entries,
                motion = motion,
                layout = MappingLayouts.matching(console, body.side, entries, motion, saved),
            )
        }
}
