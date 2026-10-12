package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/** Null until set, meaning the layout decides. */
interface SensitivityRepository {
    fun observe(console: Console, body: PlayerBody): Flow<Int?>
    suspend fun set(console: Console, body: PlayerBody, percent: Int)
}
