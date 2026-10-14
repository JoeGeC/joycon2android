package com.joegec.joycon2android.buttonmapping

/** A lone Joy-Con of one side, a full controller (pair or Pro), or the NSO GameCube controller. */
enum class JoyconSide(val isLone: Boolean = false) { LEFT(isLone = true), RIGHT(isLone = true), DUAL, GAMECUBE }
