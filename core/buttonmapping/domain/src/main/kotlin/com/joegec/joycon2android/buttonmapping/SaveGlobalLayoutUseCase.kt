package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.first

class SaveGlobalLayoutUseCase(
    private val globalLayouts: GlobalLayoutRepository,
    private val observePlayerMapping: ObservePlayerMappingUseCase,
) {
    suspend operator fun invoke(console: Console, bodies: List<PlayerBody>, name: String) {
        val snapshots = bodies.map { observePlayerMapping(console, it).first().snapshot() }
        globalLayouts.save(GlobalLayout(MappingLayouts.newId(), name, console, snapshots))
    }
}
