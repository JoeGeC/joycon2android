package com.joegec.joycon2android.settings.presentation

data class LowBatteryActions(
    val toggleNotification: (Boolean) -> Unit,
    val toggleVibration: (Boolean) -> Unit,
    val setWarnings: (first: Int, final: Int) -> Unit,
)
