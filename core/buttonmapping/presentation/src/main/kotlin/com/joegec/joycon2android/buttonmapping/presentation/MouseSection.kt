package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JOYCON_MOUSE_KEY
import com.joegec.joycon2android.buttonmapping.MOUSE_MODE_KEY
import com.joegec.joycon2android.buttonmapping.joyconMouse
import com.joegec.joycon2android.buttonmapping.joyconMouseEntry
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.components.SettingWarning
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextTitle

/** Its switch brings up the mouse for this player; the rest shows only while it's on: docs/virtual-gamepad.md#mouse */
@Composable
internal fun MouseSection(
    console: Console,
    targets: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    controller: LiveController,
    actions: MappingActions,
    warnings: List<String>,
) {
    val on = state.mapping.joyconMouse(console)
    val switchChanged = JOYCON_MOUSE_KEY in state.changes.targets
    val changed = switchChanged || MOUSE_MODE_KEY in state.changes.targets || state.anyChanged(targets)
    MappingSectionBox(MappingSection.MOUSE, changed) {
        SettingSwitch(
            title = stringResource(R.string.mouse_on),
            description = stringResource(R.string.mouse_on_description),
            checked = on,
            titleColor = changedColor(switchChanged, TextTitle),
            onCheckedChange = { actions.setMapping(state.body, JOYCON_MOUSE_KEY, joyconMouseEntry(console, it)) },
        )
        AnimatedVisibility(
            visible = on,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
                warnings.forEach { SettingWarning(it) }
                val sources = MappingOptions.sources(state.body.side, MappingSection.MOUSE)
                targets.forEach { (key, label) -> BindingRow(label, key, sources, state, controller, actions) }
                MouseModeRow(sources, state, controller, actions)
            }
        }
    }
}
