package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.MOUSE_MODE_KEY
import com.joegec.joycon2android.buttonmapping.sourceIdOf
import com.joegec.joycon2android.buttonmapping.sourceIdsOf
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.DropdownTrigger
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/** The combo a player holds to switch their Joy-Con mouse off or on: docs/virtual-gamepad.md#mouse-on-and-off */
@Composable
internal fun MouseModeRow(
    sources: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    controller: LiveController,
    actions: MappingActions,
) {
    val label = stringResource(R.string.mouse_mode_target)
    val names = sourceIdsOf(state.mapping[MOUSE_MODE_KEY].orEmpty())
        .mapNotNull { id -> sources.firstOrNull { it.first == id }?.second }
    var listening by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = changedColor(MOUSE_MODE_KEY in state.changes.targets, TextDim), modifier = Modifier.weight(1f))
        Box(Modifier.weight(1f)) {
            DropdownTrigger(
                label = names.takeIf { it.isNotEmpty() }?.joinToString(" + ") ?: stringResource(R.string.source_none),
                subLabel = if (names.isEmpty()) null else stringResource(R.string.mouse_mode_hold),
            ) { listening = true }
        }
    }
    if (listening) {
        PressComboDialog(
            targetLabel = label,
            controller = controller,
            side = state.body.side,
            heldSideways = state.wiiController.heldSideways,
            offeredIds = sources.mapTo(mutableSetOf()) { it.first } - MappingOptions.NONE_ID,
            pauseDeviceKeys = actions.setDeviceKeysPaused,
            onCombo = { ids ->
                listening = false
                actions.setMapping(state.body, MOUSE_MODE_KEY, sourceIdOf(ids))
                actions.confirmPress(controller.player)
            },
            onClear = {
                listening = false
                actions.setMapping(state.body, MOUSE_MODE_KEY, "")
            },
            onDismiss = { listening = false },
        )
    }
}
