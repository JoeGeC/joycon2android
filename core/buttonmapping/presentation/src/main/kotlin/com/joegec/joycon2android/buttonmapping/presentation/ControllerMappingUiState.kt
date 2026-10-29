package com.joegec.joycon2android.buttonmapping.presentation

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.MappingLayout
import com.joegec.joycon2android.buttonmapping.MappingLayouts
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.PlayerMapping
import com.joegec.joycon2android.buttonmapping.SavedLayout
import com.joegec.joycon2android.buttonmapping.WiiController
import com.joegec.joycon2android.buttonmapping.proController
import com.joegec.joycon2android.buttonmapping.rumblePercent

data class ControllerMappingUiState(
    val console: Console,
    val global: GlobalMapping,
    val players: List<PlayerMappingUiState>,
    val savedLayouts: List<SavedLayout>,
)

data class PlayerMappingUiState(
    val body: PlayerBody,
    val layouts: List<MappingLayout>,
    val layout: MappingLayout?,
    val wiiController: WiiController,
    /** Empty off the Wii. */
    val wiiControllers: List<WiiController>,
    val thrust: Boolean,
    val offersThrust: Boolean,
    val sensitivityPercent: Int,
    val offersSensitivity: Boolean,
    val proController: Boolean,
    val offersProController: Boolean,
    val rumblePercent: Int,
    val offersRumble: Boolean,
    val mapping: Map<String, String>,
) {
    val singleJoycon: Boolean get() = offersProController && !proController
    val classicController: Boolean get() = wiiController == WiiController.CLASSIC
}

internal fun controllerMappingUiState(
    console: Console,
    mapping: GlobalMapping,
    savedLayouts: List<SavedLayout>,
) = ControllerMappingUiState(
    console = console,
    global = mapping,
    players = mapping.players.map {
        it.uiState(console, MappingLayouts.forBody(console, it.body.side, savedLayouts))
    },
    savedLayouts = savedLayouts,
)

private fun PlayerMapping.uiState(
    console: Console,
    layouts: List<MappingLayout>,
) = PlayerMappingUiState(
    body = body,
    layouts = layouts,
    layout = layout,
    wiiController = motion.controller,
    wiiControllers = MappingOptions.wiiControllers(console, body.side),
    thrust = motion.thrust,
    offersThrust = MappingOptions.offersThrust(console, motion.controller),
    sensitivityPercent = motion.sensitivityPercent,
    offersSensitivity = console == Console.WIIMOTE_NUNCHUK,
    proController = entries.proController,
    offersProController = MappingOptions.offersProController(console, body.side),
    rumblePercent = entries.rumblePercent,
    offersRumble = MappingOptions.offersRumble(console),
    mapping = entries,
)

internal fun ControllerMappingUiState.takenNames(session: Boolean): List<String> =
    if (session) global.savedLayouts.map { it.name }
    else players.flatMap { it.layouts }.filterIsInstance<SavedLayout>().map { it.name }.distinct()
