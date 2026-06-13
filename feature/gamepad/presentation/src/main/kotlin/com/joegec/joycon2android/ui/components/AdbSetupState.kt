package com.joegec.joycon2android.ui.components

import com.joegec.joycon2android.uhid.AdbState

data class AdbSetupState(
    val needed: Boolean = false,
    val state: AdbState = AdbState.DISCONNECTED,
    val error: String? = null,
    val notificationsGranted: Boolean = true,
)
