package com.joegec.joycon2android.buttonmapping.target

/**
 * Prefixed because the names are storage keys, shared with [WiimoteButton]'s on the same body. Ends with
 * the remote's Shake and Recenter while a Classic Controller is plugged in, kept apart from the remote's own.
 */
enum class ClassicButton {
    ClassicA,
    ClassicB,
    ClassicX,
    ClassicY,
    ClassicL,
    ClassicR,
    ClassicZL,
    ClassicZR,
    ClassicPlus,
    ClassicMinus,
    ClassicHome,
    ClassicDPadUp,
    ClassicDPadDown,
    ClassicDPadLeft,
    ClassicDPadRight,
    ClassicShake,
    ClassicRecenter,
}
