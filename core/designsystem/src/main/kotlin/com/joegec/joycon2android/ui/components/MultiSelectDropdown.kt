package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/** Stays open while choices are ticked. The first option is the caller's "none", which empties the row and closes. */
@Composable
fun MultiSelectDropdown(
    options: List<Pair<String, String>>,
    selectedIds: List<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val none = options.firstOrNull() ?: return
    val label = selectedIds.mapNotNull { id -> options.firstOrNull { it.first == id }?.second }
        .takeIf { it.isNotEmpty() }
        ?.joinToString(", ")
        ?: none.second
    var expanded by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier.onSizeChanged { anchorWidth = with(density) { it.width.toDp() } }) {
        DropdownTrigger(label, subLabel = null) { expanded = true }
        PanelDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            options = options.map { (id, text) -> DropdownOption(id, text) },
            selectedId = null,
            ticked = selectedIds.toSet(),
            // A source's name runs longer than an emulator's, so the panel may outgrow its trigger.
            modifier = Modifier.widthIn(min = anchorWidth),
            onSelect = { option ->
                onToggle(option.id)
                if (option.id == none.first) expanded = false
            },
        )
    }
}
