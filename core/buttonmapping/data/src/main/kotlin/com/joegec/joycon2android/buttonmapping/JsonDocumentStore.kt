package com.joegec.joycon2android.buttonmapping

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class JsonDocumentStore<T>(
    private val dataStore: DataStore<Preferences>,
    private val encode: (T) -> String,
    private val decode: (id: String, json: String) -> T?,
) {
    fun observe(): Flow<List<T>> = dataStore.data.map { prefs ->
        prefs.asMap().entries.mapNotNull { (key, value) -> decode(key.name, value.toString()) }
    }

    suspend fun save(id: String, document: T) {
        dataStore.edit { it[stringPreferencesKey(id)] = encode(document) }
    }

    suspend fun delete(id: String) {
        dataStore.edit { it.remove(stringPreferencesKey(id)) }
    }
}
