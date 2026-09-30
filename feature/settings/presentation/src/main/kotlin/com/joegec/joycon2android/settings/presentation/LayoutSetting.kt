package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.joegec.joycon2android.model.ConnectionViewMode
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
internal fun LayoutSetting(
    mode: ConnectionViewMode,
    onModeChange: (ConnectionViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        Text(
            stringResource(R.string.settings_layout_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        ) {
            ConnectionViewMode.entries.forEach { option ->
                LayoutOption(
                    icon = option.icon(),
                    label = option.label(),
                    selected = option == mode,
                    onClick = { onModeChange(option) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun LayoutOption(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) TextOnAccent else TextDim
    Column(
        modifier
            .clip(RoundedCornerShape(Dimens.buttonCorner))
            .background(if (selected) Accent else ButtonOff)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(Dimens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        Icon(icon, contentDescription = null, tint = contentColor)
        Text(label, color = contentColor, style = MaterialTheme.typography.labelLarge)
    }
}

private fun ConnectionViewMode.icon(): ImageVector = when (this) {
    ConnectionViewMode.DETAILED -> Icons.Filled.ViewAgenda
    ConnectionViewMode.COMPACT -> Icons.AutoMirrored.Filled.ViewList
}

@Composable
private fun ConnectionViewMode.label(): String = when (this) {
    ConnectionViewMode.DETAILED -> stringResource(R.string.settings_layout_detailed)
    ConnectionViewMode.COMPACT -> stringResource(R.string.settings_layout_compact)
}
