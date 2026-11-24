package com.joegec.joycon2android.battery

import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.PlayerNumber

data class ControllerBattery(
    val address: String,
    val player: PlayerNumber?,
    val model: ControllerModel,
    val deviceName: String,
    val percent: Int,
)

fun AppUiState.controllerBatteries(): List<ControllerBattery> =
    unassignedJoycons.mapNotNull { it.battery(player = null) } +
        players.flatMap { player -> listOfNotNull(player.left, player.right).mapNotNull { it.battery(player.player) } }

private fun ConnectedJoycon.battery(player: PlayerNumber?): ControllerBattery? =
    input.battery?.let { ControllerBattery(address, player, model, deviceName, it.percent) }
