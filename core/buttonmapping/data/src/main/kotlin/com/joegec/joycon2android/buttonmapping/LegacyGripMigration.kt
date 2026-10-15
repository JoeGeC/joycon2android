package com.joegec.joycon2android.buttonmapping

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

// Outside every body's key prefix, so no mapping ever reads it.
private val MIGRATED = booleanPreferencesKey("legacy_grip_migrated")

/** Keeps a lone Joy-Con mapped before the Pro Controller switch on the sideways grip it was set up for. */
internal class LegacyGripMigration : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences) = currentData[MIGRATED] != true

    override suspend fun migrate(currentData: Preferences): Preferences {
        val migrated = currentData.toMutablePreferences()
        ObserveSingleJoyconBodiesUseCase.LONE_BODIES.forEach { body ->
            val prefix = "${bodyKeyPrefix(Console.SWITCH_PRO, body)}|"
            val stored = currentData.asMap().entries
                .filter { (key, _) -> key.name.startsWith(prefix) }
                .associate { (key, value) -> key.name.removePrefix(prefix) to value.toString() }
            if (stored.isEmpty()) return@forEach
            (stored.withLegacyGrip(Console.SWITCH_PRO, body.side) - stored.keys).forEach { (target, source) ->
                migrated[stringPreferencesKey(prefix + target)] = source
            }
        }
        migrated[MIGRATED] = true
        return migrated
    }

    override suspend fun cleanUp() = Unit
}
