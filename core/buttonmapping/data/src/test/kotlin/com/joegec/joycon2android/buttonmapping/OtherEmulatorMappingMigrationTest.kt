package com.joegec.joycon2android.buttonmapping

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class OtherEmulatorMappingMigrationTest {

    private val migration = OtherEmulatorMappingMigration()
    private val androidHome = stringPreferencesKey("ANDROID|P1|DUAL|ANDROID_HOME")
    private val emulatorHome = stringPreferencesKey("EMULATOR|P1|DUAL|ANDROID_HOME")
    private val edenA = stringPreferencesKey("SWITCH_PRO|P1|DUAL|A")

    @Test
    fun `copies the Android mapping to other emulators, leaving both it and the rest`() = runBlocking {
        val migrated = migration.migrate(preferencesOf(androidHome to "Home", edenA to "B"))

        assertEquals("Home", migrated[emulatorHome])
        assertEquals("Home", migrated[androidHome])
        assertNull(migrated[stringPreferencesKey("EMULATOR|P1|DUAL|A")])
    }

    @Test
    fun `copies once, so Android keys mapped later stay Android's`() = runBlocking {
        val migrated = migration.migrate(preferencesOf())

        assertFalse(migration.shouldMigrate(migrated))
    }
}
