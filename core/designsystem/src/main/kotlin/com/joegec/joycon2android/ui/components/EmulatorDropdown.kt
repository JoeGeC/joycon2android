package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
fun EmulatorDropdown(
    options: List<EmulatorOption>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = options.firstOrNull { it.id == selectedId } ?: options.firstOrNull() ?: return

    if (options.size == 1) {
        Text(
            selected.label,
            color = Accent,
            style = MaterialTheme.typography.labelMedium,
            modifier = modifier.padding(Dimens.emulatorPickerPadding),
        )
        return
    }

    OptionDropdown(
        options = options.map { DropdownOption(it.id, it.label) },
        selectedId = selected.id,
        label = selected.label,
        onSelect = onSelect,
        modifier = modifier,
    )
}
