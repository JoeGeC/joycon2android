package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.targetKey
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.KeyboardKey
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.OptionDropdown
import com.joegec.joycon2android.ui.components.SettingWarning

/** Lists only the keys in use, since a row for every key would bury them: docs/DESIGN.md#mapping */
@Composable
internal fun KeyboardBindingSection(
    targets: List<Pair<String, String>>,
    state: PlayerMappingUiState,
    controller: LiveController,
    actions: MappingActions,
    needsShizuku: Boolean,
    open: Boolean,
) {
    var added by rememberSaveable(state.body) { mutableStateOf(listOf<String>()) }
    val bound = targets.filter { (key, _) -> state.mapping[key].orEmpty().isNotEmpty() }.map { it.first }.toSet()
    val shown = targets.filter { (key, _) -> key in bound || key in added }

    MappingSectionBox(MappingSection.KEYBOARD, open) {
        if (needsShizuku) SettingWarning(stringResource(R.string.keyboard_keys_need_shizuku))
        if (LETTER_KEYS.any { it in bound }) SettingWarning(stringResource(R.string.keyboard_letters_hide_ime))
        val sources = MappingOptions.sources(state.body.side, MappingSection.KEYBOARD)
        shown.forEach { (key, label) -> BindingRow(label, key, sources, state, controller, actions) }
        OptionDropdown(
            options = (targets - shown.toSet()).map { (key, label) -> DropdownOption(key, label) },
            selectedId = null,
            label = stringResource(R.string.keyboard_add_key),
            onSelect = { added = added + it },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val LETTER_KEYS = KeyboardKey.entries.filter { it.isLetter }.map { it.targetKey }.toSet()
