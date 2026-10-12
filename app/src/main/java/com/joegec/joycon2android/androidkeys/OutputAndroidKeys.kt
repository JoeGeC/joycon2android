package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.model.AndroidKey
import com.joegec.joycon2android.model.AndroidKeyBindings
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

class OutputAndroidKeys(
    private val enabled: StateFlow<Boolean>,
    private val bindings: StateFlow<Map<PlayerNumber, AndroidKeyBindings>>,
) {
    val isEnabled: Boolean get() = enabled.value

    fun of(player: PlayerState): AndroidKeyBindings = bindings.value[player.player] ?: AndroidKeyBindings()

    fun heldKeys(player: PlayerState): Set<AndroidKey> = if (isEnabled) of(player).heldKeys(player) else emptySet()
}
