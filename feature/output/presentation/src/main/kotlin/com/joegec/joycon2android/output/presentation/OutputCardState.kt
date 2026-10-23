package com.joegec.joycon2android.output.presentation

import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputSelection
import com.joegec.joycon2android.ui.components.DolphinSetupPhase

data class OutputCardState(
    val running: Boolean = false,
    val selection: OutputSelection = OutputSelection(),
    val emulators: List<EmulatorTarget> = emptyList(),
    val selectedEmulator: String? = null,
    val setupPhase: DolphinSetupPhase = DolphinSetupPhase.IDLE,
    /** What is up now, which a failed start leaves short of what the mode asks for. */
    val liveDevices: Set<OutputDevice> = emptySet(),
    val error: String? = null,
)
