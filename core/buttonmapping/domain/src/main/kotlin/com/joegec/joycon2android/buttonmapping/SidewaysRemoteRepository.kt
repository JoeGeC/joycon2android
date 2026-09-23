package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/** Null until set, meaning the layout decides. */
interface SidewaysRemoteRepository {
    fun observe(console: Console, body: PlayerBody): Flow<Boolean?>
    suspend fun set(console: Console, body: PlayerBody, enabled: Boolean)
}
