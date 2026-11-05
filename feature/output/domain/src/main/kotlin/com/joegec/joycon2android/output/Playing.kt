package com.joegec.joycon2android.output

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.output.OutputDevice.KEYBOARD
import com.joegec.joycon2android.output.OutputDevice.MOTION
import com.joegec.joycon2android.output.OutputDevice.MOUSE
import com.joegec.joycon2android.output.OutputDevice.PAD

/**
 * What the Output card is set to: the devices the game reads, the console it maps, and the
 * emulator Set up writes to, if any. docs/architecture.md#output
 */
enum class Playing(
    val console: Console,
    val needs: Set<OutputDevice>,
    val emulator: EmulatorKind? = null,
    val motionOptional: Boolean = false,
    val buttonsOverDsu: Boolean = false,
) {
    SWITCH(Console.SWITCH_PRO, setOf(PAD, MOTION), EmulatorKind.EDEN, motionOptional = true, buttonsOverDsu = true),
    GAMECUBE(Console.GAMECUBE, setOf(PAD), EmulatorKind.DOLPHIN_GAMECUBE),
    WII(Console.WIIMOTE_NUNCHUK, setOf(MOTION), EmulatorKind.DOLPHIN_WII, buttonsOverDsu = true),
    OTHER_EMULATOR(Console.EMULATOR, setOf(PAD, MOTION), motionOptional = true, buttonsOverDsu = true),
    ANDROID(Console.ANDROID, setOf(PAD)),
    PC(Console.PC, setOf(KEYBOARD, MOUSE)),
}
