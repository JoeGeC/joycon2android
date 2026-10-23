package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.PlayerState

data class GamepadFrame(val state: PlayerState, val androidKeys: Set<AndroidKey> = emptySet()) {
    companion object {
        /** Screenshot goes through the keyboard or shell instead: docs/virtual-gamepad.md#android-keys */
        val SENDABLE_KEYS = setOf(AndroidKey.HOME, AndroidKey.BACK)
    }
}
