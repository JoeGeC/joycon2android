package com.joegec.joycon2android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launchDataStore: DataStore<Preferences> by preferencesDataStore(name = "launch_preferences")

class MainScreenLaunchDataStore(context: Context) : MainScreenLaunchRepository {

    private val dataStore = context.applicationContext.launchDataStore

    override val launchOnMainScreen: Flow<Boolean> = dataStore.data.map { it[LAUNCH_ON_MAIN_SCREEN] ?: true }

    override suspend fun setLaunchOnMainScreen(enabled: Boolean) {
        dataStore.edit { it[LAUNCH_ON_MAIN_SCREEN] = enabled }
    }

    private companion object {
        val LAUNCH_ON_MAIN_SCREEN = booleanPreferencesKey("launch_on_main_screen")
    }
}
