package com.joegec.joycon2android.connection

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.joegec.joycon2android.model.ConnectionViewMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.viewPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "view_preferences")

class ViewModePreferencesDataStore(context: Context) : ViewModePreferences {

    private val dataStore = context.applicationContext.viewPreferencesDataStore

    override val viewMode: Flow<ConnectionViewMode> = dataStore.data.map { prefs ->
        prefs[KEY]?.let { name -> runCatching { ConnectionViewMode.valueOf(name) }.getOrNull() }
            ?: ConnectionViewMode.DETAILED
    }

    override suspend fun setViewMode(mode: ConnectionViewMode) {
        dataStore.edit { it[KEY] = mode.name }
    }

    private companion object {
        val KEY = stringPreferencesKey("connection_view_mode")
    }
}
