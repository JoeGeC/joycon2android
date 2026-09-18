package com.joegec.joycon2android.update

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException

private val Context.updatePreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_preferences")

class UpdatePreferencesDataStore(context: Context) : SkippedVersionRepository {

    private val dataStore = context.applicationContext.updatePreferencesDataStore

    override suspend fun skippedVersion(): AppVersion? {
        val stored = dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .first()[SKIPPED_VERSION]
        return stored?.let(AppVersion::parse)
    }

    override suspend fun skip(version: AppVersion) {
        dataStore.edit { it[SKIPPED_VERSION] = version.toString() }
    }

    private companion object {
        val SKIPPED_VERSION = stringPreferencesKey("skipped_version")
    }
}
