package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.RemoteMotion
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextTitle

/** How a Wii player's motion is shaped, then what Shake and Recenter are bound to. Classic has only sensitivity. */
@Composable
internal fun MotionSection(
    targets: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    controller: LiveController,
    actions: MappingActions,
) {
    val changed = state.changes.thrust || state.changes.sensitivity || state.anyChanged(targets)
    MappingSectionBox(MappingSection.MOTION, changed) {
        if (state.offersThrust) ThrustSwitch(state.thrust, state.changes.thrust) { actions.setThrust(state.body, it) }
        if (state.offersSensitivity) {
            SensitivitySlider(state.sensitivityPercent, state.changes.sensitivity) { actions.setSensitivity(state.body, it) }
        }
        val sources = MappingOptions.sources(state.body.side, MappingSection.MOTION)
        targets.forEach { (key, label) -> BindingRow(label, key, sources, state, controller, actions) }
    }
}

/** docs/dsu-motion.md#thrusts */
@Composable
private fun ThrustSwitch(enabled: Boolean, changed: Boolean, onSetEnabled: (Boolean) -> Unit) {
    SettingSwitch(
        title = stringResource(R.string.controller_mapping_thrust),
        titleColor = changedColor(changed, TextTitle),
        description = stringResource(R.string.controller_mapping_thrust_description),
        checked = enabled,
        onCheckedChange = onSetEnabled,
        modifier = Modifier.padding(bottom = Dimens.mappingSwitchBottomPadding),
    )
}

/** docs/dsu-motion.md#motion-sensitivity */
@Composable
private fun SensitivitySlider(percent: Int, changed: Boolean, onSetPercent: (Int) -> Unit) {
    PercentSlider(
        title = stringResource(R.string.controller_mapping_sensitivity),
        titleColor = changedColor(changed, TextTitle),
        description = stringResource(R.string.controller_mapping_sensitivity_description),
        percent = percent,
        range = RemoteMotion.SENSITIVITY_PERCENT_RANGE,
        step = RemoteMotion.SENSITIVITY_PERCENT_STEP,
        valueLabel = { stringResource(R.string.controller_mapping_percent_value, it) },
        onSetPercent = onSetPercent,
    )
}
