package com.joegec.joycon2android.connectionloss

import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.PlayerNumber

data class LostController(
    val address: String,
    val player: PlayerNumber?,
    val model: ControllerModel,
    val deviceName: String,
    val ranOutOfBattery: Boolean,
)

fun AppUiState.lostControllers(): List<LostController> =
    controllersWithPlayers().filter { (joycon, _) -> joycon.connectionLost }.map { (joycon, player) ->
        LostController(
            address = joycon.address,
            player = player,
            model = joycon.model,
            deviceName = joycon.deviceName,
            ranOutOfBattery = joycon.connectionState.batteryAtLoss?.flat == true,
        )
    }

fun AppUiState.connectedAddresses(): Set<String> =
    controllersWithPlayers()
        .filter { (joycon, _) -> joycon.connectionState.connected }
        .mapTo(mutableSetOf()) { (joycon, _) -> joycon.address }

private fun AppUiState.controllersWithPlayers(): List<Pair<ConnectedJoycon, PlayerNumber?>> =
    unassignedJoycons.map { it to null } +
        players.flatMap { player -> listOfNotNull(player.left, player.right).map { it to player.player } }
