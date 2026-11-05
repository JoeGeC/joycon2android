package com.joegec.joycon2android.output.presentation

import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.output.PlayingSelection
import com.joegec.joycon2android.output.selected
import com.joegec.joycon2android.ui.components.DolphinSetupPhase

data class OutputCardState(
    /** Off until the first player is assigned. */
    val on: Boolean = false,
    val selection: PlayingSelection = PlayingSelection(),
    val emulators: Map<Playing, List<EmulatorTarget>> = emptyMap(),
    val setupPhase: DolphinSetupPhase = DolphinSetupPhase.IDLE,
    /** What the plan asks for, and what is up now, which a failed start leaves short. */
    val plannedDevices: Set<OutputDevice> = emptySet(),
    val liveDevices: Set<OutputDevice> = emptySet(),
    /** A device's own failure, such as the pad's failed start. */
    val errors: Map<OutputDevice, String> = emptyMap(),
    /** Where an emulator set up by hand finds DSU. */
    val dsuAddress: String = "",
    /** Some controller is connected, but none has the Joy-Con 2's mouse sensor. */
    val noPointerController: Boolean = false,
    val shizukuAvailable: Boolean = true,
    /** Players who held their combo to switch their Joy-Con mouse off. */
    val mouseOffPlayers: Set<PlayerNumber> = emptySet(),
) {
    val playing: Playing get() = selection.playing

    val targets: List<EmulatorTarget> get() = emulators[playing].orEmpty()

    val target: EmulatorTarget? get() = targets.selected(selection.emulatorId)

    val error: String? get() = errors.entries.firstOrNull { (device, _) -> device in plannedDevices }?.value
}
