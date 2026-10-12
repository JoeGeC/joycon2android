package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.thrustDataStore: DataStore<Preferences> by preferencesDataStore(name = "thrust")

class ThrustDataStore(context: Context) : ThrustRepository {

    private val dataStore = context.applicationContext.thrustDataStore

    override fun observe(console: Console, body: PlayerBody): Flow<Boolean?> =
        dataStore.data.map { it[preferenceKey(console, body)] }

    override suspend fun set(console: Console, body: PlayerBody, enabled: Boolean) {
        dataStore.edit { it[preferenceKey(console, body)] = enabled }
    }

    private fun preferenceKey(console: Console, body: PlayerBody) =
        booleanPreferencesKey(bodyKeyPrefix(console, body))
}
