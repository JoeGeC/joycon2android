package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

/**
 * A shipped layout goes to every player; saved player layouts sharing a name give each player the
 * one saved for their body; a saved set restores each player's own frozen bindings.
 */
class ApplyGlobalLayoutUseCase(
    private val globalLayouts: GlobalLayoutRepository,
    private val savedLayouts: SavedLayoutRepository,
    private val applyMappingLayout: ApplyMappingLayoutUseCase,
    private val applyPlayerMapping: ApplyPlayerMappingUseCase,
) {
    suspend operator fun invoke(console: Console, bodies: List<PlayerBody>, layoutId: String) {
        globalLayouts.observe().first().firstOrNull { it.id == layoutId }?.let { saved ->
            saved.bodies.forEach { applyPlayerMapping(console, it.body, it.entries, it.motion) }
            return
        }
        val named = MappingLayouts.namedAlike(savedLayouts.observe().first(), console, layoutId)
        if (named.isEmpty()) {
            bodies.forEach { applyMappingLayout(console, it, layoutId) }
        } else {
            bodies.forEach { body ->
                named.firstOrNull { it.side == body.side }?.let { applyMappingLayout(console, body, it.id) }
            }
        }
    }
}
