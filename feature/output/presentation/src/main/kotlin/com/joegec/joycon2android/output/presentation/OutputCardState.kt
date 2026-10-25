package com.joegec.joycon2android.output.presentation

import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputMode
import com.joegec.joycon2android.output.OutputSelection
import com.joegec.joycon2android.output.selected
import com.joegec.joycon2android.ui.components.DolphinSetupPhase

data class OutputCardState(
    val running: Boolean = false,
    val selection: OutputSelection = OutputSelection(),
    val emulators: Map<OutputMode, List<EmulatorTarget>> = emptyMap(),
    val setupPhase: DolphinSetupPhase = DolphinSetupPhase.IDLE,
    /** What is up now, which a failed start leaves short of what the tab asks for. */
    val liveDevices: Set<OutputDevice> = emptySet(),
    val error: String? = null,
    /** DSU's own line for the subtitle on the Motion tab: its port and listening emulators. */
    val motionDetail: String? = null,
    /** Some controller is connected, but none has the Joy-Con 2's mouse sensor. */
    val noPointerController: Boolean = false,
) {
    fun emulatorsFor(mode: OutputMode): List<EmulatorTarget> = emulators[mode].orEmpty()

    fun selectedEmulatorFor(mode: OutputMode): String? = emulatorsFor(mode).selected(selection.emulators[mode])?.id
}
