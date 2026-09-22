package com.joegec.joycon2android.buttonmapping.target

/**
 * A Wii Remote's own buttons, its Nunchuk's two, and the one thing on it that is not a button at
 * all: [Shake], the jerk of the remote that a game like Mario Kart Wii reads as a trick. It sits
 * here because the editor binds sources to it exactly as it does to a button.
 */
enum class WiimoteButton {
    A,
    B,
    One,
    Two,
    Home,
    Plus,
    Minus,
    DPadUp,
    DPadDown,
    DPadLeft,
    DPadRight,
    NunchukC,
    NunchukZ,
    Shake,
}
