package com.joegec.joycon2android.buttonmapping

/** Which physical body a mapping applies to: a lone Joy-Con of one side, or a full controller. */
enum class JoyconSide(val displayName: String, val shortName: String) {
    LEFT("Left Joy-Con", "L"),
    RIGHT("Right Joy-Con", "R"),
    DUAL("Dual Joy-Cons / Pro Controller", "L/R"),
}
