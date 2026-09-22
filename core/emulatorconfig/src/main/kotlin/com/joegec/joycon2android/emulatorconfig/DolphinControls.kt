package com.joegec.joycon2android.emulatorconfig

import com.joegec.joycon2android.buttonmapping.StickDirection

/**
 * The spellings Dolphin's ini uses, shared by the two generators that write one. These are wire
 * tokens rather than anything a user reads — Dolphin will not match a key spelled otherwise.
 */
object DolphinControls {
    val DIRECTIONS = mapOf(
        StickDirection.UP to "Up",
        StickDirection.DOWN to "Down",
        StickDirection.LEFT to "Left",
        StickDirection.RIGHT to "Right",
    )
}
