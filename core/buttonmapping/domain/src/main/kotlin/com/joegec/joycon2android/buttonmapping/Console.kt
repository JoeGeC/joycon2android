package com.joegec.joycon2android.buttonmapping

/** A distinct controller shape an emulator can present to the user, independent of which emulator. */
enum class Console(val displayName: String) {
    GAMECUBE("Gamecube"),
    WIIMOTE_NUNCHUK("Wiimote & Nunchuck"),
    SWITCH_PRO("Joycons"),
}
