package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/** Null until set, meaning the layout decides. */
interface WiiControllerRepository {
    fun observe(console: Console, body: PlayerBody): Flow<WiiController?>
    suspend fun set(console: Console, body: PlayerBody, controller: WiiController)
}
