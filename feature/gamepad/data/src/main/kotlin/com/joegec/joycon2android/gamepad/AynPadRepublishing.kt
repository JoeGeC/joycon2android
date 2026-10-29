package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell

/** AYN firmware swaps a new pad for a copy without rumble: docs/virtual-gamepad.md#rumble */
internal object AynPadRepublishing {

    private const val SETTING = "no_create_gamepad_button_layout"

    // Read only at a pad's creation, and reset by the firmware at the next app switch. Absent elsewhere.
    fun skipNextPad(shell: PrivilegedShell) {
        shell.shell("[ \"$(settings get system $SETTING)\" = 0 ] && settings put system $SETTING 1")?.waitFor()
    }
}
