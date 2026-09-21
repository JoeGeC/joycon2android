package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/**
 * Stores the user's overrides to the Joy-Con button mapping, keyed by console shape and body.
 * Values are opaque strings (the [MappingSource] ids driving one target, joined by [sourceIdOf], or
 * a legacy whole-stick [StickSource] name) — this layer knows nothing about what a key or value
 * means, only how to persist it; the presets and use cases give them meaning.
 */
interface ControllerMappingRepository {
    fun observe(console: Console, side: JoyconSide): Flow<Map<String, String>>
    suspend fun set(console: Console, side: JoyconSide, targetKey: String, sourceId: String)
    suspend fun clear(console: Console, side: JoyconSide)
}
