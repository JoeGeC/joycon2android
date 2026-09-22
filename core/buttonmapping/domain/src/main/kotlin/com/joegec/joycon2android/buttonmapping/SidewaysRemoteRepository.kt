package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/** The body's answer to whether it plays as a sideways Wii Remote; null until anything has set it. */
interface SidewaysRemoteRepository {
    fun observe(console: Console, body: PlayerBody): Flow<Boolean?>
    suspend fun set(console: Console, body: PlayerBody, enabled: Boolean)
}
