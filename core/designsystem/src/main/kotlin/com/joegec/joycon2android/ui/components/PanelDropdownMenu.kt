package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun PanelDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<Pair<String, String>>,
    selectedId: String,
    onSelect: (id: String) -> Unit,
    modifier: Modifier = Modifier,
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
        options.forEach { (id, label) ->
            DropdownMenuItem(
                text = {
                    Text(
                        label,
                        color = if (id == selectedId) Accent else Color.White,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                onClick = { onSelect(id) },
            )
        }
    }
}
