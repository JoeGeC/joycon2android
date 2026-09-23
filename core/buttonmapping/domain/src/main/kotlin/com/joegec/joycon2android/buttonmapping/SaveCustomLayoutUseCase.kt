package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

/** Applies nothing: the bindings already match, so the card picks up the name. */
class SaveCustomLayoutUseCase(
    private val savedLayouts: SavedLayoutRepository,
    private val observePlayerMapping: ObservePlayerMappingUseCase,
) {
    suspend operator fun invoke(console: Console, body: PlayerBody, name: String) {
        val current = observePlayerMapping(console, body).first()
        savedLayouts.save(
            SavedLayout(
                id = MappingLayouts.newId(),
                name = name,
                console = console,
                side = body.side,
                bindings = current.entries,
                sidewaysRemote = current.sidewaysRemote,
            ),
        )
    }
}
