package com.joegec.joycon2android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tipsDataStore: DataStore<Preferences> by preferencesDataStore(name = "tips_preferences")

class TipsDataStore(context: Context) : TipsRepository {

    private val dataStore = context.applicationContext.tipsDataStore

    override val showTips: Flow<Boolean> = dataStore.data.map { it[SHOW_TIPS] ?: true }

    override suspend fun setShowTips(enabled: Boolean) {
        dataStore.edit { it[SHOW_TIPS] = enabled }
    }

    private companion object {
        val SHOW_TIPS = booleanPreferencesKey("show_tips")
    }
}
