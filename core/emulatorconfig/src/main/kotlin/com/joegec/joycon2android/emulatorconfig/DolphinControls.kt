package com.joegec.joycon2android.emulatorconfig

import com.joegec.joycon2android.buttonmapping.StickDirection

/** Wire tokens: Dolphin won't match a key spelled otherwise. */
object DolphinControls {
    val DIRECTIONS = mapOf(
        StickDirection.UP to "Up",
        StickDirection.DOWN to "Down",
        StickDirection.LEFT to "Left",
        StickDirection.RIGHT to "Right",
    )
}
