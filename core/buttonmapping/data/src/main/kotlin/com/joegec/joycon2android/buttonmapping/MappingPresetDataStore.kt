package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.mappingPresetDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "mapping_preset")

class MappingPresetDataStore(context: Context) : MappingPresetRepository {

    private val dataStore = context.applicationContext.mappingPresetDataStore

    override fun observe(console: Console): Flow<String?> = dataStore.data.map { it[preferenceKey(console)] }

    override suspend fun set(console: Console, presetId: String) {
        dataStore.edit { it[preferenceKey(console)] = presetId }
    }

    private fun preferenceKey(console: Console) = stringPreferencesKey(console.name)
}
