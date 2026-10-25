package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Named for the Sideways switch it replaced, so stored choices carry over.
private val Context.wiiControllerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "sideways_remote",
    produceMigrations = { context ->
        listOf(PerPlayerMigration(::perPlayerSidewaysNames), WiiControllerMigration(context.controllerMappingDataStore))
    },
)

// The switch used to be keyed by the console alone, for every player and body at once.
private fun perPlayerSidewaysNames(legacyName: String): List<String>? =
    segmentsOf(legacyName).singleOrNull()
        ?.let(::consoleNamed)
        ?.let { everyBodyKey(it) }

class WiiControllerDataStore(context: Context) : WiiControllerRepository {

    private val dataStore = context.applicationContext.wiiControllerDataStore

    override fun observe(console: Console, body: PlayerBody): Flow<WiiController?> =
        dataStore.data.map { prefs ->
            prefs[preferenceKey(console, body)]?.let { name -> WiiController.entries.firstOrNull { it.name == name } }
        }

    override suspend fun set(console: Console, body: PlayerBody, controller: WiiController) {
        dataStore.edit { it[preferenceKey(console, body)] = controller.name }
    }

    private fun preferenceKey(console: Console, body: PlayerBody) =
        stringPreferencesKey(bodyKeyPrefix(console, body))
}
