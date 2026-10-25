package com.joegec.joycon2android.buttonmapping

import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

// Outside every body's key prefix, so no body ever reads it.
private val MIGRATED = booleanPreferencesKey("wii_controller_migrated")

/** Turns the Sideways switch and the Classic Controller binding entry into one stored [WiiController]. */
internal class WiiControllerMigration(
    private val mappings: DataStore<Preferences>,
) : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences) = currentData[MIGRATED] != true

    override suspend fun migrate(currentData: Preferences): Preferences {
        val migrated = currentData.toMutablePreferences()
        currentData.asMap().forEach { (key, value) ->
            if (value !is Boolean) return@forEach
            migrated.remove(key)
            val controller = if (value) WiiController.SIDEWAYS_REMOTE else WiiController.REMOTE
            migrated[stringPreferencesKey(key.name)] = controller.name
        }
        classicBodyPrefixes().forEach { migrated[stringPreferencesKey(it)] = WiiController.CLASSIC.name }
        migrated[MIGRATED] = true
        return migrated
    }

    private suspend fun classicBodyPrefixes(): List<String> {
        val suffix = "|$LEGACY_CLASSIC_CONTROLLER_KEY"
        return mappings.data.first().asMap()
            .filter { (key, value) -> key.name.endsWith(suffix) && value == "true" }
            .map { (key, _) -> key.name.removeSuffix(suffix) }
    }

    override suspend fun cleanUp() = Unit
}
