package com.joegec.joycon2android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.unsavedMappingWarningDataStore: DataStore<Preferences> by preferencesDataStore(name = "unsaved_mapping_warning")

class UnsavedMappingWarningDataStore(context: Context) : UnsavedMappingWarningRepository {

    private val dataStore = context.applicationContext.unsavedMappingWarningDataStore

    override val warnUnsavedMapping: Flow<Boolean> = dataStore.data.map { it[WARN] ?: true }

    override suspend fun setWarnUnsavedMapping(enabled: Boolean) {
        dataStore.edit { it[WARN] = enabled }
    }

    private companion object {
        val WARN = booleanPreferencesKey("warn_unsaved_mapping")
    }
}
