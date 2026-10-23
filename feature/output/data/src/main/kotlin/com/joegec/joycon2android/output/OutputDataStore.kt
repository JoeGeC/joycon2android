package com.joegec.joycon2android.output

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

private val Context.outputDataStore: DataStore<Preferences> by preferencesDataStore(name = "output")

class OutputDataStore(context: Context) : OutputRepository {

    private val dataStore = context.applicationContext.outputDataStore
    private val _running = MutableStateFlow(false)

    override val running: StateFlow<Boolean> = _running.asStateFlow()

    override val selection: Flow<OutputSelection> = dataStore.data.map { prefs ->
        OutputSelection(
            mode = prefs[MODE]?.let(::modeOrNull) ?: OutputSelection().mode,
            emulatorId = prefs[EMULATOR],
            overrides = OutputDevice.entries.mapNotNull { device ->
                prefs[overrideKey(device)]?.let(::overrideOrNull)?.let { device to it }
            }.toMap(),
        )
    }

    override suspend fun setMode(mode: OutputMode) {
        dataStore.edit { it[MODE] = mode.name }
    }

    override suspend fun selectEmulator(id: String) {
        dataStore.edit { it[EMULATOR] = id }
    }

    override suspend fun setOverride(device: OutputDevice, override: DeviceOverride) {
        dataStore.edit { it[overrideKey(device)] = override.name }
    }

    override fun setRunning(running: Boolean) {
        _running.value = running
    }

    // A name from a later build reads as the default rather than failing.
    private fun modeOrNull(name: String) = OutputMode.entries.firstOrNull { it.name == name }
    private fun overrideOrNull(name: String) = DeviceOverride.entries.firstOrNull { it.name == name }

    private companion object {
        val MODE = stringPreferencesKey("mode")
        val EMULATOR = stringPreferencesKey("emulator")
        fun overrideKey(device: OutputDevice) = stringPreferencesKey("override_${device.name}")
    }
}
