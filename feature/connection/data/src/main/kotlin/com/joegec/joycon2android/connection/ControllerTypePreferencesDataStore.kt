package com.joegec.joycon2android.connection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.controllerTypeDataStore: DataStore<Preferences> by preferencesDataStore(name = "controller_types")

class ControllerTypePreferencesDataStore(context: Context) : ControllerTypePreferences {

    private val dataStore = context.applicationContext.controllerTypeDataStore

    override val overrides: Flow<Map<String, Side>> = dataStore.data.map { prefs ->
        prefs.asMap().mapNotNull { (key, value) ->
            val side = runCatching { Side.valueOf(value as String) }.getOrNull() ?: return@mapNotNull null
            key.name to side
        }.toMap()
    }

    override suspend fun set(address: String, side: Side?) {
        dataStore.edit { prefs ->
            val key = stringPreferencesKey(address)
            if (side == null) prefs.remove(key) else prefs[key] = side.name
        }
    }
}
