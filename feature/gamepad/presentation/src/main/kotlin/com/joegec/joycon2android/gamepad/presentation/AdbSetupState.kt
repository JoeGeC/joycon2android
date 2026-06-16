package com.joegec.joycon2android.gamepad.presentation

import com.joegec.joycon2android.gamepad.wirelessdebug.AdbState

data class AdbSetupState(
    val needed: Boolean = false,
    val state: AdbState = AdbState.DISCONNECTED,
    val error: String? = null,
    val notificationsGranted: Boolean = true,
)
