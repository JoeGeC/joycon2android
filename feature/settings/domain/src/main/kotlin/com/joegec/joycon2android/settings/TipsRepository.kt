package com.joegec.joycon2android.settings

import kotlinx.coroutines.flow.Flow

interface TipsRepository {
    val showTips: Flow<Boolean>
    suspend fun setShowTips(enabled: Boolean)
}
