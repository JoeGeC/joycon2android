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
            when {
                targets.isEmpty() -> Unit
                section == MappingSection.KEYBOARD ->
                    KeyboardBindingSection(targets, state, actions, warnings.keyboardNeedsShizuku)
                else -> BindingSection(section, targets, state, actions, warnings.androidNeedsShizuku)
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
    androidKeysNeedShizuku: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        SectionHeader(section)
        if (section == MappingSection.ANDROID && androidKeysNeedShizuku) {
            SettingWarning(stringResource(R.string.android_keys_need_shizuku))
        }
        val sources = MappingOptions.sources(state.body.side, section)
        targets.forEach { (key, label) -> BindingRow(label, key, sources, state, actions) }
    }
}
