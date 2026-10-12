package com.joegec.joycon2android.emulator

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.emulatorconfig.EdenPaths

object EmulatorConsoles {
    fun forGamepad(emulatorId: String): Console =
        if (emulatorId in EdenPaths.PACKAGES) Console.SWITCH_PRO else Console.GAMECUBE

    fun forDsu(emulatorId: String): Console =
        if (emulatorId in EdenPaths.PACKAGES) Console.SWITCH_PRO else Console.WIIMOTE_NUNCHUK
}
