package com.joegec.joycon2android.mouse

import com.joegec.joycon2android.androidkeys.OutputBindings
import com.joegec.joycon2android.buttonmapping.HoldCombo
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Holding a player's mouse mode combo switches their Joy-Con mouse off or back on: docs/virtual-gamepad.md#mouse-on-and-off */
class MouseModeToggle(
    private val combos: OutputBindings<HoldCombo>,
    private val nowMillis: () -> Long,
    private val onToggled: (PlayerState) -> Unit,
) {
    private val _off = MutableStateFlow(emptySet<PlayerNumber>())
    val off: StateFlow<Set<PlayerNumber>> = _off.asStateFlow()
    private val heldSince = mutableMapOf<PlayerNumber, Long>()

    // Toggled this hold, so holding on doesn't toggle again.
    private val latched = mutableSetOf<PlayerNumber>()

    fun onFrame(players: List<PlayerState>) {
        val present = players.mapTo(mutableSetOf()) { it.player }
        heldSince.keys.retainAll(present)
        latched.retainAll(present)
        _off.update { it intersect present }
        val now = nowMillis()
        players.forEach { onFrame(it, now) }
    }

    private fun onFrame(player: PlayerState, now: Long) {
        val id = player.player
        if (combos.of(player).sources.keys.none { it.heldBy(player) }) {
            heldSince -= id
            latched -= id
            return
        }
        if (id in latched) return
        if (now - heldSince.getOrPut(id) { now } < HOLD_MILLIS) return
        latched += id
        _off.update { if (id in it) it - id else it + id }
        onToggled(player)
    }

    companion object {
        const val HOLD_MILLIS = 1_000L
    }
}
