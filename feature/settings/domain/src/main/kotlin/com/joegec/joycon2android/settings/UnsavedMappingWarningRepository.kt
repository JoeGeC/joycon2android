package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

interface UnsavedMappingWarningRepository {
    val warnUnsavedMapping: Flow<Boolean>
    suspend fun setWarnUnsavedMapping(enabled: Boolean)
}
