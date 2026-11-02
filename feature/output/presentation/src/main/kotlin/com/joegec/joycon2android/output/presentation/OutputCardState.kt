package com.joegec.joycon2android.output.presentation

import com.joegec.joycon2android.output.DeviceKeyKind
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputMode
import com.joegec.joycon2android.output.OutputPlan
import com.joegec.joycon2android.output.OutputSelection
import com.joegec.joycon2android.output.selected
import com.joegec.joycon2android.ui.components.DolphinSetupPhase

data class OutputCardState(
    val running: Set<OutputMode> = emptySet(),
    val selection: OutputSelection = OutputSelection(),
    val emulators: Map<OutputMode, List<EmulatorTarget>> = emptyMap(),
    val setupPhase: DolphinSetupPhase = DolphinSetupPhase.IDLE,
    /** What is up now, which a failed start leaves short of what the running tabs ask for. */
    val liveDevices: Set<OutputDevice> = emptySet(),
    /** A tab's own failure, such as the pad's failed start. */
    val errors: Map<OutputMode, String> = emptyMap(),
    /** DSU's own line for the Motion tab's status: its port and listening emulators. */
    val motionDetail: String? = null,
    /** Some controller is connected, but none has the Joy-Con 2's mouse sensor. */
    val noPointerController: Boolean = false,
    val shizukuAvailable: Boolean = true,
    /** What the running tabs' mappings press besides their own outputs. */
    val mappedKeys: Set<DeviceKeyKind> = emptySet(),
    /** The running tabs, Mouse & keys aside, whose mappings press keys or click. */
    val tabsMappingKeys: Set<OutputMode> = emptySet(),
) {
    fun emulatorsFor(mode: OutputMode): List<EmulatorTarget> = emulators[mode].orEmpty()

    fun selectedEmulatorFor(mode: OutputMode): String? = selectedTargetFor(mode)?.id

    fun isLit(mode: OutputMode): Boolean = mode.devices.any { it in liveDevices }

    val setupsCollide: Boolean
        get() = OutputMode.GAMEPAD in running && OutputMode.MOTION in running &&
            OutputPlan.setupsCollide(selectedTargetFor(OutputMode.GAMEPAD), selectedTargetFor(OutputMode.MOTION))

    private fun selectedTargetFor(mode: OutputMode) = emulatorsFor(mode).selected(selection.emulators[mode])
}
