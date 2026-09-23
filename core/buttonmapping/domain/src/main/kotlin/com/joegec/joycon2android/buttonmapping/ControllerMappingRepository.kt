package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/** Values are opaque strings ([sourceIdsOf]); the layouts and use cases give them meaning. */
interface ControllerMappingRepository {
    fun observe(console: Console, body: PlayerBody): Flow<Map<String, String>>
    suspend fun set(console: Console, body: PlayerBody, targetKey: String, sourceId: String)

    /** Swaps a body's whole mapping in one write, so a layout never lands half-applied. */
    suspend fun replace(console: Console, body: PlayerBody, entries: Map<String, String>)
}
