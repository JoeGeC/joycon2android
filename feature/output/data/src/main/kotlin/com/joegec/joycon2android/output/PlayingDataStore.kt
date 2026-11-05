package com.joegec.joycon2android.output

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** [targetOf] is the emulator a per-device tab had, for [PlayingMigration] on first read. */
class PlayingDataStore(context: Context, private val targetOf: (OutputMode, String?) -> EmulatorTarget?) : PlayingRepository {

    private val dataStore = context.applicationContext.outputDataStore
    private val _on = MutableStateFlow(false)

    override val on: StateFlow<Boolean> = _on.asStateFlow()

    override val selection: Flow<PlayingSelection> = dataStore.data.map { prefs ->
        val playing = prefs[PLAYING]?.let(::playingOrNull) ?: return@map migrated(prefs)
        PlayingSelection(
            playing = playing,
            emulators = Playing.entries.mapNotNull { game -> prefs[emulatorKey(game)]?.let { game to it } }.toMap(),
            motion = prefs[MOTION] ?: true,
        )
    }

    override suspend fun setPlaying(playing: Playing) = edit { it[PLAYING] = playing.name }

    override suspend fun selectEmulator(playing: Playing, id: String) = edit { it[emulatorKey(playing)] = id }

    override suspend fun setMotion(on: Boolean) = edit { it[MOTION] = on }

    override suspend fun setOn(on: Boolean) {
        _on.value = on
        edit { it[REMEMBERED_ON] = on }
    }

    override suspend fun startRemembered() {
        val prefs = dataStore.data.first()
        if (prefs[REMEMBERED_ON] ?: PlayingMigration.wasOn(rememberedTabs(prefs))) _on.value = true
    }

    override fun stopAll() {
        _on.value = false
    }

    // Writes the migrated choice first, so every later edit lands on it.
    private suspend fun edit(change: (MutablePreferences) -> Unit) {
        dataStore.edit { prefs ->
            if (prefs[PLAYING] == null) prefs.storeMigrated()
            change(prefs)
        }
    }

    private fun MutablePreferences.storeMigrated() {
        val migrated = migrated(this)
        this[PLAYING] = migrated.playing.name
        migrated.emulators.forEach { (game, id) -> this[emulatorKey(game)] = id }
        this[MOTION] = migrated.motion
        this[REMEMBERED_ON] = PlayingMigration.wasOn(rememberedTabs(this))
    }

    private fun migrated(prefs: Preferences): PlayingSelection {
        val inView = prefs[TAB_IN_VIEW]?.let(::modeOrNull) ?: OutputMode.GAMEPAD
        return PlayingMigration.selection(rememberedTabs(prefs), inView) { mode -> targetOf(mode, prefs[tabEmulatorKey(mode)]) }
    }

    private fun rememberedTabs(prefs: Preferences): Set<OutputMode>? = prefs[REMEMBERED_TABS]?.mapNotNullTo(mutableSetOf(), ::modeOrNull)

    // A name from another build reads as unset rather than failing.
    private fun playingOrNull(name: String) = Playing.entries.firstOrNull { it.name == name }
    private fun modeOrNull(name: String) = OutputMode.entries.firstOrNull { it.name == name }

    private companion object {
        val PLAYING = stringPreferencesKey("playing")
        val MOTION = booleanPreferencesKey("playing_motion")
        val REMEMBERED_ON = booleanPreferencesKey("playing_on")
        fun emulatorKey(playing: Playing) = stringPreferencesKey("playing_emulator_${playing.name}")

        // The per-device tabs' keys, read only to migrate.
        val TAB_IN_VIEW = stringPreferencesKey("mode")
        val REMEMBERED_TABS = stringSetPreferencesKey("running")
        fun tabEmulatorKey(mode: OutputMode) = stringPreferencesKey("emulator_${mode.name}")
    }
}
