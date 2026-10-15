package com.joegec.joycon2android.buttonmapping

/** Stored among a body's bindings, so layouts save it: docs/virtual-gamepad.md#single-joy-con */
const val PRO_CONTROLLER_KEY = "PRO_CONTROLLER"

const val PRO_CONTROLLER_ON = "true"

val Map<String, String>.proController: Boolean get() = this[PRO_CONTROLLER_KEY] == PRO_CONTROLLER_ON

/** Eden sees a lone Joy-Con as itself unless its player picked Pro Controller. */
fun Map<String, String>.isSingleJoycon(console: Console, side: JoyconSide): Boolean =
    console == Console.SWITCH_PRO && side.isLone && !proController
