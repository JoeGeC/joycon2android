package com.joegec.joycon2android.buttonmapping

/**
 * Stored among a body's bindings, so layouts save it. The default is stored empty, which layout
 * matching ignores, so a body at the default still answers to its layout. docs/virtual-gamepad.md#rumble
 */
const val RUMBLE_PERCENT_KEY = "RUMBLE_PERCENT"

/** Of the actuator's safe maximum. 20% is the strength that felt right on a Joy-Con 2. */
const val DEFAULT_RUMBLE_PERCENT = 20
val RUMBLE_PERCENT_RANGE = 0..100
const val RUMBLE_PERCENT_STEP = 5

val Map<String, String>.rumblePercent: Int
    get() = this[RUMBLE_PERCENT_KEY]?.toIntOrNull()?.coerceIn(RUMBLE_PERCENT_RANGE) ?: DEFAULT_RUMBLE_PERCENT
