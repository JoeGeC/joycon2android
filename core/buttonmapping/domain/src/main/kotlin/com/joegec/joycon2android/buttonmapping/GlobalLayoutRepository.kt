package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/** Stores the whole-session layouts the user saved, across every console. */
interface GlobalLayoutRepository {
    fun observe(): Flow<List<GlobalLayout>>
    suspend fun save(layout: GlobalLayout)
    suspend fun delete(layoutId: String)
}
