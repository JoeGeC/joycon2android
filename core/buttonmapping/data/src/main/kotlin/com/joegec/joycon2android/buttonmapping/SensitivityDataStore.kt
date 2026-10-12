package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sensitivityDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "motion_sensitivity",
)

class SensitivityDataStore(context: Context) : SensitivityRepository {

    private val dataStore = context.applicationContext.sensitivityDataStore

    override fun observe(console: Console, body: PlayerBody): Flow<Int?> =
        dataStore.data.map { it[preferenceKey(console, body)] }

    override suspend fun set(console: Console, body: PlayerBody, percent: Int) {
        dataStore.edit { it[preferenceKey(console, body)] = percent }
    }

    private fun preferenceKey(console: Console, body: PlayerBody) =
        intPreferencesKey(bodyKeyPrefix(console, body))
}
