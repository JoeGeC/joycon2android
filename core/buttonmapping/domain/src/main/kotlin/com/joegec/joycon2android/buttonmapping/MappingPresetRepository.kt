package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/**
 * Stores which layout each console is set to, as an opaque preset id — null until the user picks
 * one. [com.joegec.joycon2android.buttonmapping.preset.MappingPresets] gives the id meaning.
 */
interface MappingPresetRepository {
    fun observe(console: Console): Flow<String?>
    suspend fun set(console: Console, presetId: String)
}
