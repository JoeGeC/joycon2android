package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Immutable
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.WiiController
import com.joegec.joycon2android.model.PlayerState

@Immutable
class MappingActions(
    val selectLayout: (body: PlayerBody, layoutId: String) -> Unit,
    val selectGlobalLayout: (layoutId: String) -> Unit,
    val saveLayout: (body: PlayerBody?, name: String) -> Unit,
    val deleteLayout: (layoutId: String, global: Boolean) -> Unit,
    val setMapping: (body: PlayerBody, targetKey: String, sourceId: String) -> Unit,
    val setWiiController: (body: PlayerBody, controller: WiiController) -> Unit,
    val setThrust: (body: PlayerBody, enabled: Boolean) -> Unit,
    val setSensitivity: (body: PlayerBody, percent: Int) -> Unit,
    val setRumble: (body: PlayerBody, percent: Int) -> Unit,
    val setProController: (body: PlayerBody, enabled: Boolean) -> Unit,
    val setDeviceKeysPaused: (paused: Boolean) -> Unit,
    val confirmPress: (player: PlayerState) -> Unit,
)
