package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console

/** Which devices run, which mapping they read, and which setup an emulator gets: docs/architecture.md#output */
object OutputPlan {

    /** [mapped] is what the active console's mapping presses: the keyboard for its keys, the mouse for clicks. */
    fun devices(selection: OutputSelection, emulator: EmulatorKind?, mapped: Set<OutputDevice>): Set<OutputDevice> =
        OutputDevice.entries.filterTo(mutableSetOf()) { device ->
            when (selection.override(device)) {
                DeviceOverride.ON -> true
                DeviceOverride.OFF -> false
                DeviceOverride.AUTO -> auto(device, selection.mode, emulator) || device in mapped
            }
        }

    fun console(mode: OutputMode, emulator: EmulatorKind?): Console = when (mode) {
        OutputMode.CONTROLLER -> emulator?.console ?: Console.ANDROID
        OutputMode.MOUSE_KEYBOARD -> Console.PC
    }

    /**
     * The device whose emulator config Set up writes; null when it would reach the emulator through
     * neither, or the target is one the player sets up by hand.
     */
    fun setUpThrough(emulator: EmulatorKind, devices: Set<OutputDevice>): OutputDevice? = when (emulator) {
        EmulatorKind.DOLPHIN_GAMECUBE -> OutputDevice.PAD
        EmulatorKind.DOLPHIN_WII -> OutputDevice.MOTION
        EmulatorKind.EDEN -> when {
            OutputDevice.MOTION in devices -> OutputDevice.MOTION
            OutputDevice.PAD in devices -> OutputDevice.PAD
            else -> null
        }
        EmulatorKind.OTHER_GAMEPAD, EmulatorKind.OTHER_MOTION -> null
    }

    private fun auto(device: OutputDevice, mode: OutputMode, emulator: EmulatorKind?) = when (device) {
        OutputDevice.PAD, OutputDevice.MOTION -> mode == OutputMode.CONTROLLER && emulator.runs(device)
        OutputDevice.KEYBOARD, OutputDevice.MOUSE -> mode == OutputMode.MOUSE_KEYBOARD
    }

    private fun EmulatorKind?.runs(device: OutputDevice) = this != null && device in devices
}
