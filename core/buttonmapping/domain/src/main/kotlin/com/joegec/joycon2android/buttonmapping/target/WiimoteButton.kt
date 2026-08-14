package com.joegec.joycon2android.buttonmapping.target

/**
 * A Wii Remote's own buttons plus its Nunchuk's two buttons. The Nunchuk entries only apply when
 * a full body (Wii Remote + Nunchuk) is connected — a lone Joy-Con has no extension.
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
}
