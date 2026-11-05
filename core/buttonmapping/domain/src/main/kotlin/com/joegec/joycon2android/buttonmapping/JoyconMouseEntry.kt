package com.joegec.joycon2android.buttonmapping

/** Stored among a body's bindings, so layouts save it: docs/virtual-gamepad.md#mouse */
const val JOYCON_MOUSE_KEY = "JOYCON_MOUSE"

/** Unset, on for Mouse & keys alone. */
fun Map<String, String>.joyconMouse(console: Console): Boolean = when (this[JOYCON_MOUSE_KEY]) {
    true.toString() -> true
    false.toString() -> false
    else -> console.joyconMouseByDefault
}

private val Console.joyconMouseByDefault get() = this == Console.PC

/** What to store for [on]: empty where it matches the default, so the body still matches its layout. */
fun joyconMouseEntry(console: Console, on: Boolean): String =
    if (on == console.joyconMouseByDefault) "" else on.toString()
