package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/**
 * The app's picker: an accented current value that opens a panel of alternatives. [label] is shown
 * rather than derived, so a caller whose state has drifted off the list can say so in its own words.
 */
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
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimens.buttonCorner))
                .clickable { expanded = true }
                .heightIn(min = Dimens.minTouchTarget)
                .padding(horizontal = Dimens.emulatorPickerPadding),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, color = Accent, style = MaterialTheme.typography.labelMedium)
                subLabel?.let { Text(it, color = TextDim, style = MaterialTheme.typography.labelSmall) }
            }
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Accent)
        }
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
