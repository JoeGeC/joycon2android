package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console

/** The console an emulator's mapping targets: docs/architecture.md#output */
enum class EmulatorKind(val console: Console) {
    DOLPHIN_GAMECUBE(Console.GAMECUBE),
    DOLPHIN_WII(Console.WIIMOTE_NUNCHUK),
    EDEN(Console.SWITCH_PRO),

    /** Nothing to set up: Android games, or an emulator set up in its own settings. Maps only the app's keys. */
    OTHER(Console.ANDROID),
}
