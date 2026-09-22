package com.joegec.joycon2android.buttonmapping.target

/**
 * A Wii Remote's own buttons, its Nunchuk's two, and the one thing on it that is not a button at
 * all: [Shake], the jerk of the remote that a game like Mario Kart Wii reads as a trick. It sits
 * here because the editor binds sources to it exactly as it does to a button.
 */
enum class WiimoteButton(val displayName: String) {
    A("A"),
    B("B"),
    One("1"),
    Two("2"),
    Home("Home"),
    Plus("+"),
    Minus("-"),
    DPadUp("D-Pad Up"),
    DPadDown("D-Pad Down"),
    DPadLeft("D-Pad Left"),
    DPadRight("D-Pad Right"),
    NunchukC("Nunchuk C"),
    NunchukZ("Nunchuk Z"),
    Shake("Shake"),
}
