package com.joegec.joycon2android.buttonmapping

/** Which physical body a mapping applies to: a lone Joy-Con of one side, or a full controller. */
enum class JoyconSide(val displayName: String) {
    LEFT("Left Joy-Con"),
    RIGHT("Right Joy-Con"),
    DUAL("Dual Joy-Cons / Pro Controller"),
}
