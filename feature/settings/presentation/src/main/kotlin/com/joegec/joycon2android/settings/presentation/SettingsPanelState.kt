package com.joegec.joycon2android.settings.presentation

import com.joegec.joycon2android.model.ConnectionViewMode
import com.joegec.joycon2android.settings.OutputSettings

data class SettingsPanelState(
    val viewMode: ConnectionViewMode = ConnectionViewMode.DETAILED,
    val outputSettings: OutputSettings = OutputSettings(),
    val deviceMotionBlockAvailable: Boolean = false,
)
