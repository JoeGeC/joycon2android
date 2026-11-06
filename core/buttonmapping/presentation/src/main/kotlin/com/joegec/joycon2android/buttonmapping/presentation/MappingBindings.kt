package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.RUMBLE_PERCENT_KEY
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.SettingWarning
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
fun MappingBindings(
    console: Console,
    state: PlayerMappingUiState,
    controller: LiveController,
    actions: MappingActions,
    warnings: DeviceKeyWarnings = DeviceKeyWarnings(),
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing)) {
        MappingSection.entries.forEach { section ->
            val targets = MappingOptions.targets(console, section, state.body.side, state.singleJoycon, state.classicController)
            when {
                section == MappingSection.RUMBLE ->
                    if (state.offersRumble) RumbleSection(console, state.rumblePercent, RUMBLE_PERCENT_KEY in state.changes.targets) { actions.setRumble(state.body, it) }
                section == MappingSection.MOTION ->
                    if (targets.isNotEmpty() || state.offersSensitivity) MotionSection(targets, state, controller, actions)
                targets.isEmpty() -> Unit
                section == MappingSection.MOUSE ->
                    MouseSection(console, targets, state, controller, actions, sectionWarnings(section, state, warnings))
                section == MappingSection.KEYBOARD ->
                    KeyboardBindingSection(targets, state, controller, actions, warnings.keyboardAndMouseNeedShizuku)
                else -> BindingSection(section, targets, state, controller, actions, sectionWarnings(section, state, warnings))
            }
        }
    }
}

@Composable
private fun BindingSection(
    section: MappingSection,
    targets: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    controller: LiveController,
    actions: MappingActions,
    warnings: List<String>,
) {
    MappingSectionBox(section, changed = state.anyChanged(targets)) {
        warnings.forEach { SettingWarning(it) }
        val sources = MappingOptions.sources(state.body.side, section)
        targets.forEach { (key, label) -> BindingRow(label, key, sources, state, controller, actions) }
    }
}

@Composable
private fun sectionWarnings(
    section: MappingSection,
    state: PlayerMappingUiState,
    warnings: DeviceKeyWarnings,
): List<String> = buildList {
    when (section) {
        MappingSection.ANDROID -> if (warnings.androidNeedsShizuku) add(stringResource(R.string.android_keys_need_shizuku))
        MappingSection.MOUSE -> {
            if (warnings.keyboardAndMouseNeedShizuku) add(stringResource(R.string.mouse_needs_shizuku))
            if (state.body in warnings.pointerlessBodies) add(stringResource(R.string.mouse_no_pointer))
        }
        else -> Unit
    }
}
