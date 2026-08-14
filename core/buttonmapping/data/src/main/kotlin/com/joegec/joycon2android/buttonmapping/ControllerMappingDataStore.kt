package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.controllerMappingDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "controller_mapping")

class ControllerMappingDataStore(context: Context) : ControllerMappingRepository {

    private val dataStore = context.applicationContext.controllerMappingDataStore

    override fun observe(console: Console, side: JoyconSide): Flow<Map<String, String>> =
        dataStore.data.map { prefs ->
            val prefix = keyPrefix(console, side)
            prefs.asMap().entries
                .filter { (key, _) -> key.name.startsWith(prefix) }
                .associate { (key, value) -> key.name.removePrefix(prefix) to value.toString() }
        }

    override suspend fun set(console: Console, side: JoyconSide, targetKey: String, sourceId: String) {
        dataStore.edit { it[preferenceKey(console, side, targetKey)] = sourceId }
    }

    override suspend fun clear(console: Console, side: JoyconSide) {
        val prefix = keyPrefix(console, side)
        dataStore.edit { prefs ->
            prefs.asMap().keys.filter { it.name.startsWith(prefix) }.forEach { prefs.remove(it) }
        }
    }

    private fun keyPrefix(console: Console, side: JoyconSide) = "${console.name}|${side.name}|"

    private fun preferenceKey(console: Console, side: JoyconSide, targetKey: String) =
        stringPreferencesKey(keyPrefix(console, side) + targetKey)
}
