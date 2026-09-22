package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.globalLayoutDataStore: DataStore<Preferences> by preferencesDataStore(name = "global_layouts")

class GlobalLayoutDataStore(context: Context) : GlobalLayoutRepository {

    private val documents = JsonDocumentStore(
        context.applicationContext.globalLayoutDataStore,
        encode = GlobalLayout::toJson,
        decode = ::globalLayoutOf,
    )

    override fun observe(): Flow<List<GlobalLayout>> =
        documents.observe().map { layouts -> layouts.sortedBy { it.displayName } }

    override suspend fun save(layout: GlobalLayout) = documents.save(layout.id, layout)

    override suspend fun delete(layoutId: String) = documents.delete(layoutId)
}
