package com.joegec.joycon2android.output

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// One per file per process, so the tab and game stores share it.
internal val Context.outputDataStore: DataStore<Preferences> by preferencesDataStore(name = "output")
