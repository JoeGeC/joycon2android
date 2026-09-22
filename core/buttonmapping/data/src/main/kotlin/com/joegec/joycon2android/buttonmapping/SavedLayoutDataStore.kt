package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.savedLayoutDataStore: DataStore<Preferences> by preferencesDataStore(name = "saved_layouts")

class SavedLayoutDataStore(context: Context) : SavedLayoutRepository {

    private val documents = JsonDocumentStore(
        context.applicationContext.savedLayoutDataStore,
        encode = SavedLayout::toJson,
        decode = ::savedLayoutOf,
    )

    override fun observe(): Flow<List<SavedLayout>> =
        documents.observe().map { layouts -> layouts.sortedBy { it.displayName } }

    override suspend fun save(layout: SavedLayout) = documents.save(layout.id, layout)

    override suspend fun delete(layoutId: String) = documents.delete(layoutId)
}
