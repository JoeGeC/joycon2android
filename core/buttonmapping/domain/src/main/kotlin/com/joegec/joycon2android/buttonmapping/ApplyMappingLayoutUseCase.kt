package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

class ApplyMappingLayoutUseCase(
    private val savedLayouts: SavedLayoutRepository,
    private val applyPlayerMapping: ApplyPlayerMappingUseCase,
) {
    suspend operator fun invoke(console: Console, body: PlayerBody, layoutId: String) {
        val layout = MappingLayouts.byId(console, body.side, layoutId, savedLayouts.observe().first())
        applyPlayerMapping(
            console,
            body,
            MappingLayouts.entriesOf(console, body.side, layout),
            layout.sidewaysRemote,
        )
    }
}
