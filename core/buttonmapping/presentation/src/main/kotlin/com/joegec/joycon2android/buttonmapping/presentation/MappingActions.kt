package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Immutable
import com.joegec.joycon2android.buttonmapping.PlayerBody

@Immutable
class MappingActions(
    val selectLayout: (body: PlayerBody, layoutId: String) -> Unit,
    val selectGlobalLayout: (layoutId: String) -> Unit,
    val saveLayout: (body: PlayerBody?, name: String) -> Unit,
    val deleteLayout: (layoutId: String, global: Boolean) -> Unit,
    val setMapping: (body: PlayerBody, targetKey: String, sourceId: String) -> Unit,
    val setSidewaysRemote: (body: PlayerBody, enabled: Boolean) -> Unit,
)
