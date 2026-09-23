package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.ui.theme.Dimens

/** [label] is passed in rather than derived, so a caller whose value is off the list can say so. */
@Composable
fun OptionDropdown(
    options: List<DropdownOption>,
    selectedId: String?,
    label: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
    onUnavailable: (DropdownOption) -> Unit = {},
    onDelete: ((DropdownOption) -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier.onSizeChanged { anchorWidth = with(density) { it.width.toDp() } }) {
        DropdownTrigger(label, subLabel) { expanded = true }
        PanelDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = options,
            selectedId = selectedId,
            modifier = Modifier.width(anchorWidth),
            onDelete = onDelete?.let { delete -> { option -> expanded = false; delete(option) } },
            onSelect = { option ->
                expanded = false
                if (option.available) onSelect(option.id) else onUnavailable(option)
            },
        )
    }
}
