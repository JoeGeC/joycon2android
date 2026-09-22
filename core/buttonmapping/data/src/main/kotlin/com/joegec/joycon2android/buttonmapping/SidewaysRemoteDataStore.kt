package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sidewaysRemoteDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sideways_remote",
    produceMigrations = { listOf(PerPlayerMigration(::perPlayerSidewaysNames)) },
)

// The switch used to be keyed by the console alone, for every player and body at once.
private fun perPlayerSidewaysNames(legacyName: String): List<String>? =
    segmentsOf(legacyName).singleOrNull()
        ?.let(::consoleNamed)
        ?.let { everyBodyKey(it) }

class SidewaysRemoteDataStore(context: Context) : SidewaysRemoteRepository {

    private val dataStore = context.applicationContext.sidewaysRemoteDataStore

    override fun observe(console: Console, body: PlayerBody): Flow<Boolean?> =
        dataStore.data.map { it[preferenceKey(console, body)] }

    override suspend fun set(console: Console, body: PlayerBody, enabled: Boolean) {
        dataStore.edit { it[preferenceKey(console, body)] = enabled }
    }

    private fun preferenceKey(console: Console, body: PlayerBody) =
        booleanPreferencesKey(bodyKeyPrefix(console, body))
}
