package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveSavedLayoutsUseCase(private val savedLayouts: SavedLayoutRepository) {
    operator fun invoke(console: Console): Flow<List<SavedLayout>> =
        savedLayouts.observe().map { layouts -> layouts.filter { it.console == console } }
}
