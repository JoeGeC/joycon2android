package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

/**
 * Sets every player at once: a shipped or saved layout goes to all of them, while a saved set gives
 * each player back the bindings it froze. Those bindings stand on their own, so a set still restores
 * exactly what it saved even after the layout a player was on has been deleted — the card simply
 * reads Custom until an identical layout exists again.
 */
class ApplyGlobalLayoutUseCase(
    private val globalLayouts: GlobalLayoutRepository,
    private val applyMappingLayout: ApplyMappingLayoutUseCase,
    private val applyPlayerMapping: ApplyPlayerMappingUseCase,
) {
    suspend operator fun invoke(console: Console, bodies: List<PlayerBody>, layoutId: String) {
        val saved = globalLayouts.observe().first().firstOrNull { it.id == layoutId }
        if (saved == null) {
            bodies.forEach { applyMappingLayout(console, it, layoutId) }
        } else {
            saved.bodies.forEach { applyPlayerMapping(console, it.body, it.entries, it.sidewaysRemote) }
        }
    }
}
