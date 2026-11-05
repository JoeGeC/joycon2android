package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.GamepadState
import com.joegec.joycon2android.model.PlayerState

/** [remapped] replaces what [state] would send, for a game that can't remap the pad itself. */
data class GamepadFrame(
    val state: PlayerState,
    val androidKeys: Set<AndroidKey> = emptySet(),
    val remapped: GamepadState? = null,
) {
    val gamepad: GamepadState get() = remapped ?: state.gamepad

    companion object {
        /** Screenshot goes through the keyboard or shell instead: docs/virtual-gamepad.md#android-keys */
        val SENDABLE_KEYS = setOf(AndroidKey.HOME, AndroidKey.BACK)
    }
}
