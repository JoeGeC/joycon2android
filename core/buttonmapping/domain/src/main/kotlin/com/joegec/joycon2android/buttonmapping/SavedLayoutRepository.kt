package com.joegec.joycon2android.buttonmapping

import kotlinx.coroutines.flow.Flow

interface SavedLayoutRepository {
    fun observe(): Flow<List<SavedLayout>>
    suspend fun save(layout: SavedLayout)
    suspend fun delete(layoutId: String)
}
