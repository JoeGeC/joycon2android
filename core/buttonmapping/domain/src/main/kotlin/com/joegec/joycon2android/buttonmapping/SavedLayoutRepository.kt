package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

/** Stores the layouts the user saved for a single body, across every console. */
interface SavedLayoutRepository {
    fun observe(): Flow<List<SavedLayout>>
    suspend fun save(layout: SavedLayout)
    suspend fun delete(layoutId: String)
}
