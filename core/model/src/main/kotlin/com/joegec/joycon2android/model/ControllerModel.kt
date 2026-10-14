package com.joegec.joycon2android.model

/** From the advertised product ID: docs/protocol.md#advertising */
enum class ControllerModel(val defaultSide: Side, val hasAnalogTriggers: Boolean = false) {
    JOYCON_LEFT(Side.LEFT),
    JOYCON_RIGHT(Side.RIGHT),
    PRO_CONTROLLER(Side.PRO),
    GAMECUBE(Side.PRO, hasAnalogTriggers = true),
    UNKNOWN(Side.UNKNOWN),
}
