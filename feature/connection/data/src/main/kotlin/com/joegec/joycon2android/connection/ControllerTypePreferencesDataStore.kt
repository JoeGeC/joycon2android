package com.joegec.joycon2android.connection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joegec.joycon2android.model.ControllerModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.controllerTypeDataStore: DataStore<Preferences> by preferencesDataStore(name = "controller_types")

class ControllerTypePreferencesDataStore(context: Context) : ControllerTypePreferences {

    private val dataStore = context.applicationContext.controllerTypeDataStore

    override val overrides: Flow<Map<String, ControllerModel>> = dataStore.data.map { prefs ->
        prefs.asMap().mapNotNull { (key, value) ->
            val model = runCatching { ControllerModel.valueOf(value as String) }.getOrNull() ?: return@mapNotNull null
            key.name to model
        }.toMap()
    }

    override suspend fun set(address: String, model: ControllerModel?) {
        dataStore.edit { prefs ->
            val key = stringPreferencesKey(address)
            if (model == null) prefs.remove(key) else prefs[key] = model.name
        }
    }
}
