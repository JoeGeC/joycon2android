package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun PanelDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<DropdownOption>,
    selectedId: String?,
    onSelect: (DropdownOption) -> Unit,
    modifier: Modifier = Modifier,
    onDelete: ((DropdownOption) -> Unit)? = null,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(Dimens.buttonCorner),
        containerColor = CardBg,
        tonalElevation = 0.dp,
        border = BorderStroke(Dimens.cardBorderWidth, TextDim),
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { OptionText(option, selected = option.id == selectedId) },
                trailingIcon = deleteAction(option, onDelete),
                onClick = { onSelect(option) },
            )
        }
    }
}

@Composable
private fun OptionText(option: DropdownOption, selected: Boolean) {
    val color = when {
        !option.available -> TextDim
        selected -> Accent
        else -> Color.White
    }
    Column {
        Text(option.label, color = color, style = MaterialTheme.typography.labelMedium)
        option.subLabel?.let {
            Text(it, color = TextDim, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// Null rather than an empty composable, so a row with nothing to delete keeps no room for it.
private fun deleteAction(
    option: DropdownOption,
    onDelete: ((DropdownOption) -> Unit)?,
): (@Composable () -> Unit)? {
    if (onDelete == null || !option.deletable) return null
    return {
        IconButton(onClick = { onDelete(option) }, modifier = Modifier.size(Dimens.iconButtonSize)) {
            Icon(
                Icons.Filled.DeleteOutline,
                contentDescription = stringResource(R.string.dropdown_delete_option),
                tint = TextDim,
                modifier = Modifier.size(Dimens.iconSizeMedium),
            )
        }
    }
}
