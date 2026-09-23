package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

/** A layout goes to every player; a saved set restores each player's own frozen bindings. */
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
