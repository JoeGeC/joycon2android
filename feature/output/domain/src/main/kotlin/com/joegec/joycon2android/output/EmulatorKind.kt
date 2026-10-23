package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console

/** What a target reads, and so what [OutputPlan] runs for it: docs/architecture.md#output */
enum class EmulatorKind(val console: Console, val devices: Set<OutputDevice>) {
    DOLPHIN_GAMECUBE(Console.GAMECUBE, setOf(OutputDevice.PAD)),
    DOLPHIN_WII(Console.WIIMOTE_NUNCHUK, setOf(OutputDevice.MOTION)),

    /** DSU carries Eden's buttons and motion together, so it sees each controller once. */
    EDEN(Console.SWITCH_PRO, setOf(OutputDevice.MOTION)),

    /** Android games and any emulator, set up by hand; the app maps only its own keys. */
    OTHER_GAMEPAD(Console.ANDROID, setOf(OutputDevice.PAD)),
    OTHER_MOTION(Console.ANDROID, setOf(OutputDevice.MOTION)),
}
