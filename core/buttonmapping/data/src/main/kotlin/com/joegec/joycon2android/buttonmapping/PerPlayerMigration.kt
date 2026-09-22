package com.joegec.joycon2android.buttonmapping

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Mapping used to be one setting per console, shared by every player. Each player now holds their
 * own, so a console-wide value already stored is handed to all of them — which is what it meant.
 */
internal class PerPlayerMigration(
    private val perPlayerNames: (legacyName: String) -> List<String>?,
) : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences) =
        currentData.asMap().keys.any { perPlayerNames(it.name) != null }

    override suspend fun migrate(currentData: Preferences): Preferences {
        val migrated = currentData.toMutablePreferences()
        currentData.asMap().forEach { (key, value) ->
            val names = perPlayerNames(key.name) ?: return@forEach
            names.forEach { name -> migrated.put(name, value) }
            migrated.remove(key)
        }
        return migrated
    }

    override suspend fun cleanUp() = Unit
}

// Preferences keys carry their value type, and only these two are ever stored here.
@Suppress("UNCHECKED_CAST")
private fun MutablePreferences.put(name: String, value: Any) {
    when (value) {
        is String -> this[stringPreferencesKey(name)] = value
        is Boolean -> this[booleanPreferencesKey(name)] = value
    }
}
