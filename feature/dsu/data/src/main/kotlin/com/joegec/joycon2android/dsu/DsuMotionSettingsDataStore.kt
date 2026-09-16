package com.joegec.joycon2android.dsu

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.joegec.joycon2android.dsu.motion.DsuMotionSettings
import com.joegec.joycon2android.dsu.motion.DsuMotionSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dsuPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "dsu_preferences")

class DsuMotionSettingsDataStore(context: Context) : DsuMotionSettingsRepository {

    private val dataStore = context.applicationContext.dsuPreferencesDataStore
    private val defaults = DsuMotionSettings()

    override val settings: Flow<DsuMotionSettings> = dataStore.data.map { prefs ->
        DsuMotionSettings(
            fastMotion = prefs[FAST_MOTION] ?: defaults.fastMotion,
            blockDeviceMotion = prefs[BLOCK_DEVICE_MOTION] ?: defaults.blockDeviceMotion,
        )
    }

    override suspend fun setFastMotion(enabled: Boolean) {
        dataStore.edit { it[FAST_MOTION] = enabled }
    }

    override suspend fun setBlockDeviceMotion(enabled: Boolean) {
        dataStore.edit { it[BLOCK_DEVICE_MOTION] = enabled }
    }

    private companion object {
        val FAST_MOTION = booleanPreferencesKey("fast_motion")
        val BLOCK_DEVICE_MOTION = booleanPreferencesKey("block_device_motion")
    }
}
