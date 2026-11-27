package com.joegec.joycon2android.service

import com.joegec.joycon2android.model.AppUiState

enum class ForegroundReason { CONNECTED, SCANNING }

fun AppUiState.foregroundReason(): ForegroundReason? = when {
    anyConnected -> ForegroundReason.CONNECTED
    scanning -> ForegroundReason.SCANNING
    else -> null
}
