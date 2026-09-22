package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

/**
 * Names what a player has built so any player on that body can pick it again. Nothing is applied:
 * the bindings already *are* the layout, so the card takes the new name as soon as it exists.
 */
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
