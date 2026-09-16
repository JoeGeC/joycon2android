package com.joegec.joycon2android.dsu

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.joegec.joycon2android.dsu.motion.FastMotionPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dsuPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "dsu_preferences")

class FastMotionPreferencesDataStore(context: Context) : FastMotionPreferences {

    private val dataStore = context.applicationContext.dsuPreferencesDataStore

    override val fastMotion: Flow<Boolean> = dataStore.data.map { prefs -> prefs[KEY] ?: false }

    override suspend fun setFastMotion(enabled: Boolean) {
        dataStore.edit { it[KEY] = enabled }
    }

    private companion object {
        val KEY = booleanPreferencesKey("fast_motion")
    }
}
