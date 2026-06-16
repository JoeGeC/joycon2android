package com.joegec.joycon2android.dsu.presentation
import com.joegec.joycon2android.ui.components.DolphinSetupPhase

data class DsuCardState(
    val enabled: Boolean = false,
    val error: String? = null,
    val clientCount: Int = 0,
    val address: String? = null,
    val showSlotLimitNote: Boolean = false,
    val dolphinInstalled: Boolean = false,
    val dolphinAutoConfigAvailable: Boolean = false,
    val dolphinPhase: DolphinSetupPhase = DolphinSetupPhase.IDLE,
)
