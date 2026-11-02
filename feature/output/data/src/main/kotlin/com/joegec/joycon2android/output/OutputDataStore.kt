package com.joegec.joycon2android.output

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

private val Context.outputDataStore: DataStore<Preferences> by preferencesDataStore(name = "output")

class OutputDataStore(context: Context) : OutputRepository {

    private val dataStore = context.applicationContext.outputDataStore
    private val _running = MutableStateFlow(emptySet<OutputMode>())

    override val running: StateFlow<Set<OutputMode>> = _running.asStateFlow()

    override val selection: Flow<OutputSelection> = dataStore.data.map { prefs ->
        OutputSelection(
            mode = modeOf(prefs),
            emulators = OutputMode.entries.mapNotNull { mode -> prefs[emulatorKey(mode)]?.let { mode to it } }.toMap(),
        )
    }

    override suspend fun setMode(mode: OutputMode) {
        dataStore.edit { it[MODE] = mode.name }
    }

    override suspend fun selectEmulator(mode: OutputMode, id: String) {
        dataStore.edit { it[emulatorKey(mode)] = id }
    }

    override suspend fun setRunning(mode: OutputMode, running: Boolean) {
        _running.update { if (running) it + mode else it - mode }
        dataStore.edit { prefs ->
            val remembered = remembered(prefs)
            prefs[REMEMBERED] = (if (running) remembered + mode else remembered - mode).mapTo(mutableSetOf()) { it.name }
        }
    }

    override suspend fun startRemembered() {
        val remembered = remembered(dataStore.data.first())
        _running.update { it + remembered }
    }

    // Before tabs ran together, the one in view ran.
    override fun stopAll() {
        _running.value = emptySet()
    }

    private fun remembered(prefs: Preferences): Set<OutputMode> =
        prefs[REMEMBERED]?.mapNotNullTo(mutableSetOf(), ::modeOrNull) ?: setOf(modeOf(prefs))

    private fun modeOf(prefs: Preferences) = prefs[MODE]?.let(::modeOrNull) ?: OutputSelection().mode

    // A name from another build reads as the default rather than failing.
    private fun modeOrNull(name: String) = OutputMode.entries.firstOrNull { it.name == name }

    private companion object {
        val MODE = stringPreferencesKey("mode")
        val REMEMBERED = stringSetPreferencesKey("running")
        fun emulatorKey(mode: OutputMode) = stringPreferencesKey("emulator_${mode.name}")
    }
}
