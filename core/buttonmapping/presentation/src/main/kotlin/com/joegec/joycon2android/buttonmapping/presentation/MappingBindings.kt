package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.SettingWarning
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
fun MappingBindings(
    console: Console,
    state: PlayerMappingUiState,
    actions: MappingActions,
    warnings: DeviceKeyWarnings = DeviceKeyWarnings(),
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing)) {
        MappingSection.entries.forEach { section ->
            val targets = MappingOptions.targets(console, section, state.body.side, state.singleJoycon, state.classicController)
            val open = section.openAtFirst(console)
            when {
                targets.isEmpty() -> Unit
                section == MappingSection.KEYBOARD ->
                    KeyboardBindingSection(targets, state, actions, warnings.keyboardAndMouseNeedShizuku, open)
                else -> BindingSection(section, targets, state, actions, shizukuWarning(section, warnings), open)
            }
        }
    }
}

@Composable
private fun BindingSection(
    section: MappingSection,
    targets: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    actions: MappingActions,
    shizukuWarning: String?,
    open: Boolean,
) {
    MappingSectionBox(section, open) {
        shizukuWarning?.let { SettingWarning(it) }
        val sources = MappingOptions.sources(state.body.side, section)
        targets.forEach { (key, label) -> BindingRow(label, key, sources, state, actions) }
    }
}

// The app's own keys are extras beside an emulator's buttons; on Mouse & keys they're all there is.
private fun MappingSection.openAtFirst(console: Console) = console == Console.PC ||
    this != MappingSection.ANDROID && this != MappingSection.MOUSE && this != MappingSection.KEYBOARD

@Composable
private fun shizukuWarning(section: MappingSection, warnings: DeviceKeyWarnings): String? = when {
    section == MappingSection.ANDROID && warnings.androidNeedsShizuku -> stringResource(R.string.android_keys_need_shizuku)
    section == MappingSection.MOUSE && warnings.keyboardAndMouseNeedShizuku -> stringResource(R.string.mouse_needs_shizuku)
    else -> null
}
