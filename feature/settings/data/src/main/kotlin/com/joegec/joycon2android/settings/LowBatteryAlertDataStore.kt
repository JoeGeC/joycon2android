package com.joegec.joycon2android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lowBatteryAlertDataStore: DataStore<Preferences> by preferencesDataStore(name = "low_battery_alert")

class LowBatteryAlertDataStore(context: Context) : LowBatteryAlertRepository {

    private val dataStore = context.applicationContext.lowBatteryAlertDataStore
    private val defaults = LowBatteryAlert()

    override val alert: Flow<LowBatteryAlert> = dataStore.data.map { prefs ->
        LowBatteryAlert(
            notify = prefs[NOTIFY] ?: defaults.notify,
            vibrate = prefs[VIBRATE] ?: defaults.vibrate,
            firstWarningPercent = prefs[FIRST_WARNING_PERCENT] ?: defaults.firstWarningPercent,
            finalWarningPercent = prefs[FINAL_WARNING_PERCENT] ?: defaults.finalWarningPercent,
        )
    }

    override suspend fun setNotify(enabled: Boolean) {
        dataStore.edit { it[NOTIFY] = enabled }
    }

    override suspend fun setVibrate(enabled: Boolean) {
        dataStore.edit { it[VIBRATE] = enabled }
    }

    override suspend fun setWarningPercents(first: Int, final: Int) {
        dataStore.edit {
            it[FIRST_WARNING_PERCENT] = first
            it[FINAL_WARNING_PERCENT] = final
        }
    }

    private companion object {
        val NOTIFY = booleanPreferencesKey("notify")
        val VIBRATE = booleanPreferencesKey("vibrate")
        val FIRST_WARNING_PERCENT = intPreferencesKey("first_warning_percent")
        val FINAL_WARNING_PERCENT = intPreferencesKey("final_warning_percent")
    }
}
