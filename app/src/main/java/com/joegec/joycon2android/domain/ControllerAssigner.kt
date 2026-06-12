package com.joegec.joycon2android.domain

import com.joegec.joycon2android.ble.JoyconConnection
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber

class ControllerAssigner(
    private val assignments: PlayerAssignmentManager,
    private val connectionFor: (address: String) -> JoyconConnection?,
    private val onAssigned: (PlayerNumber) -> Unit,
    private val onUnassigned: (PlayerNumber) -> Unit,
    private val comboDetector: ComboAssignmentDetector = ComboAssignmentDetector(),
) {

    fun assign(address: String, player: PlayerNumber) {
        val connection = connectionFor(address) ?: return
        if (!assignments.assign(address, connection.side, player)) return
        connection.setPlayerLed(player)
        onAssigned(player)
    }

    fun unassign(address: String) {
        val player = assignments.getPlayer(address)
        assignments.unassign(address)
        connectionFor(address)?.clearPlayerLed()
        player?.let(onUnassigned)
    }

    fun applyCombos(unassigned: List<ConnectedJoycon>) {
        for (combo in comboDetector.detect(unassigned)) {
            val player = assignments.nextFreePlayer() ?: return
            combo.addresses.forEach { address -> assign(address, player) }
        }
    }
}
