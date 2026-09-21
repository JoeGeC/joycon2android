package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sidewaysRemoteDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "sideways_remote")

class SidewaysRemoteDataStore(context: Context) : SidewaysRemoteRepository {

    private val dataStore = context.applicationContext.sidewaysRemoteDataStore

    override fun observe(console: Console): Flow<Boolean?> = dataStore.data.map { it[preferenceKey(console)] }

    override suspend fun set(console: Console, enabled: Boolean) {
        dataStore.edit { it[preferenceKey(console)] = enabled }
    }

    override suspend fun clear(console: Console) {
        dataStore.edit { it.remove(preferenceKey(console)) }
    }

    private fun preferenceKey(console: Console) = booleanPreferencesKey(console.name)
}
