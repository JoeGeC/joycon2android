package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/**
 * The user's own answer to whether a console plays as a sideways Wii Remote, kept apart from the
 * layout that seeds it: null until they touch the switch, and cleared again when a layout is applied.
 */
interface SidewaysRemoteRepository {
    fun observe(console: Console): Flow<Boolean?>
    suspend fun set(console: Console, enabled: Boolean)
    suspend fun clear(console: Console)
}
