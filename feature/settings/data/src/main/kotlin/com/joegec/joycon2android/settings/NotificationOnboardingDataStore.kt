package com.joegec.joycon2android.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationOnboardingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "notification_onboarding",
)

class NotificationOnboardingDataStore(context: Context) : NotificationOnboardingRepository {

    private val dataStore = context.applicationContext.notificationOnboardingDataStore

    override val seen: Flow<Boolean> = dataStore.data.map { it[SEEN] ?: false }

    override suspend fun markSeen() {
        dataStore.edit { it[SEEN] = true }
    }

    private companion object {
        val SEEN = booleanPreferencesKey("seen")
    }
}
