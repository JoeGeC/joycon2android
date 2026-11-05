package com.joegec.joycon2android.buttonmapping

import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/** Android games and other emulators shared [Console.ANDROID]'s mapping; each now has its own, starting the same. */
internal class OtherEmulatorMappingMigration : DataMigration<Preferences> {

    override suspend fun shouldMigrate(currentData: Preferences) = currentData[COPIED] != true

    override suspend fun migrate(currentData: Preferences): Preferences {
        val migrated = currentData.toMutablePreferences()
        currentData.asMap().forEach { (key, value) ->
            val segments = segmentsOf(key.name)
            if (segments.first() != Console.ANDROID.name || value !is String) return@forEach
            val name = (listOf(Console.EMULATOR.name) + segments.drop(1)).joinToString(SEPARATOR)
            migrated[stringPreferencesKey(name)] = value
        }
        migrated[COPIED] = true
        return migrated
    }

    override suspend fun cleanUp() = Unit

    private companion object {
        val COPIED = booleanPreferencesKey("other_emulator_mapping_copied")
    }
}
