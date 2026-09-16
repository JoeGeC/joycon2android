package com.joegec.joycon2android.dsu.presentation
import com.joegec.joycon2android.dsu.DsuCoverage
import com.joegec.joycon2android.ui.components.DolphinSetupPhase
import com.joegec.joycon2android.ui.components.EmulatorOption

data class DsuCardState(
    val enabled: Boolean = false,
    val error: String? = null,
    val clientCount: Int = 0,
    val address: String? = null,
    val coverage: DsuCoverage = DsuCoverage(),
    val emulators: List<EmulatorOption> = emptyList(),
    val selectedEmulator: String = "",
    val setupPhase: DolphinSetupPhase = DolphinSetupPhase.IDLE,
    val fastMotion: Boolean = false,
)
