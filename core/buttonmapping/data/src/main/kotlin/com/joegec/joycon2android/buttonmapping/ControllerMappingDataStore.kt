package com.joegec.joycon2android.buttonmapping

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.controllerMappingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "controller_mapping",
    produceMigrations = { listOf(PerPlayerMigration(::perPlayerMappingNames)) },
)

// A stored override used to be keyed CONSOLE|SIDE|TARGET, for every player at once.
private fun perPlayerMappingNames(legacyName: String): List<String>? {
    val (console, side, target) = segmentsOf(legacyName).takeIf { it.size == 3 } ?: return null
    return everyPlayerKey(consoleNamed(console) ?: return null, sideNamed(side) ?: return null, target)
}

class ControllerMappingDataStore(context: Context) : ControllerMappingRepository {

    private val dataStore = context.applicationContext.controllerMappingDataStore

    override fun observe(console: Console, body: PlayerBody): Flow<Map<String, String>> =
        dataStore.data.map { prefs ->
            val prefix = keyPrefix(console, body)
            prefs.asMap().entries
                .filter { (key, _) -> key.name.startsWith(prefix) }
                .associate { (key, value) -> key.name.removePrefix(prefix) to value.toString() }
        }

    override suspend fun set(console: Console, body: PlayerBody, targetKey: String, sourceId: String) {
        dataStore.edit { it[preferenceKey(console, body, targetKey)] = sourceId }
    }

    override suspend fun replace(console: Console, body: PlayerBody, entries: Map<String, String>) {
        val prefix = keyPrefix(console, body)
        dataStore.edit { prefs ->
            prefs.asMap().keys.filter { it.name.startsWith(prefix) }.forEach { prefs.remove(it) }
            entries.forEach { (targetKey, sourceId) -> prefs[stringPreferencesKey(prefix + targetKey)] = sourceId }
        }
    }

    private fun keyPrefix(console: Console, body: PlayerBody) = "${bodyKeyPrefix(console, body)}|"

    private fun preferenceKey(console: Console, body: PlayerBody, targetKey: String) =
        stringPreferencesKey(keyPrefix(console, body) + targetKey)
}
