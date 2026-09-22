package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/**
 * Stores what each player's body is bound to, keyed by console shape and body. Values are opaque
 * strings (the [MappingSource] ids driving one target, joined by [sourceIdOf], or a legacy
 * whole-stick [StickSource] name) — this layer knows nothing about what a key or value means, only
 * how to persist it; the layouts and use cases give them meaning.
 */
interface ControllerMappingRepository {
    fun observe(console: Console, body: PlayerBody): Flow<Map<String, String>>
    suspend fun set(console: Console, body: PlayerBody, targetKey: String, sourceId: String)

    /** Swaps a body's whole mapping in one write, so a layout never lands half-applied. */
    suspend fun replace(console: Console, body: PlayerBody, entries: Map<String, String>)
}
