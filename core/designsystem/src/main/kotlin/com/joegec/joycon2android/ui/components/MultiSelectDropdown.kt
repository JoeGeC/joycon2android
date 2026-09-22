package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens

/**
 * Id/label picker for a row that can hold several choices at once. The menu stays open while they
 * are ticked off; tapping outside closes it. With nothing selected it reads as the first option,
 * which callers put there as their "none" row — picking that one empties the row, so it closes
 * rather than waiting for a tick that cannot come.
 */
@Composable
fun MultiSelectDropdown(
    options: List<Pair<String, String>>,
    selectedIds: List<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedLabels = selectedIds.mapNotNull { id -> options.firstOrNull { it.first == id }?.second }
    val label = selectedLabels.takeIf { it.isNotEmpty() }?.joinToString(", ")
        ?: options.firstOrNull()?.second
        ?: return
    var expanded by remember { mutableStateOf(false) }

    val none = options.firstOrNull()?.first

    Box(modifier) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.buttonCorner))
                .clickable { expanded = true }
                .padding(horizontal = Dimens.pillPaddingHorizontal, vertical = Dimens.pillPaddingVertical),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                label,
                color = Accent,
                fontSize = Dimens.fontSizeSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Accent)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, text) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    leadingIcon = { SelectionTick(selected = id in selectedIds) },
                    onClick = {
                        onToggle(id)
                        if (id == none) expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectionTick(selected: Boolean) {
    if (selected) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = Accent)
    } else {
        Spacer(Modifier.size(Dimens.iconSizeMedium))
    }
}
