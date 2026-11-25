package com.joegec.joycon2android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.connectionLossAlertDataStore: DataStore<Preferences> by preferencesDataStore(name = "connection_loss_alert")

class ConnectionLossAlertDataStore(context: Context) : ConnectionLossAlertRepository {

    private val dataStore = context.applicationContext.connectionLossAlertDataStore

    override val notify: Flow<Boolean> = dataStore.data.map { it[NOTIFY] ?: true }

    override suspend fun setNotify(enabled: Boolean) {
        dataStore.edit { it[NOTIFY] = enabled }
    }

    private companion object {
        val NOTIFY = booleanPreferencesKey("notify")
    }
}
