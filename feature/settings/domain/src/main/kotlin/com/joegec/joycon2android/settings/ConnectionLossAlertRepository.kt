package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

interface ConnectionLossAlertRepository {
    val notify: Flow<Boolean>
    suspend fun setNotify(enabled: Boolean)
}
