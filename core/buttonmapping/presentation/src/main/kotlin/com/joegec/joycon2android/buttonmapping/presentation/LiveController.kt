package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Immutable
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.flow.StateFlow

/** Input arrives apart from [player], so only what collects [inputs] recomposes with every frame. */
@Immutable
class LiveController(val player: PlayerState, val inputs: StateFlow<Map<String, JoyconInput>>)
