package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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

/** Generic id/label picker for rows that aren't a fixed [EmulatorOption] list, e.g. mapping editors. */
@Composable
fun LabeledDropdown(
    options: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = options.firstOrNull { it.first == selectedId } ?: options.firstOrNull() ?: return
    var expanded by remember { mutableStateOf(false) }

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
                selected.second,
                color = Accent,
                fontSize = Dimens.fontSizeSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Accent)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (id, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                )
            }
        }
    }
}
