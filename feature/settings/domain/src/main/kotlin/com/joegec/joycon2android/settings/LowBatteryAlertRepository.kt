package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

interface LowBatteryAlertRepository {
    val alert: Flow<LowBatteryAlert>
    suspend fun setNotify(enabled: Boolean)
    suspend fun setVibrate(enabled: Boolean)
    suspend fun setWarningPercents(first: Int, final: Int)
}
