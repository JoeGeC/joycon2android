package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.feature.connection.presentation.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
fun ViewModeToggle(
    mode: ConnectionViewMode,
    onModeChange: (ConnectionViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(RoundedCornerShape(Dimens.buttonCorner))
            .background(ButtonOff)
            .padding(Dimens.viewTogglePadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.viewTogglePadding),
    ) {
        ViewModeButton(
            icon = Icons.Filled.ViewAgenda,
            contentDescription = stringResource(R.string.view_detailed),
            selected = mode == ConnectionViewMode.DETAILED,
            onClick = { onModeChange(ConnectionViewMode.DETAILED) },
        )
        ViewModeButton(
            icon = Icons.AutoMirrored.Filled.ViewList,
            contentDescription = stringResource(R.string.view_compact),
            selected = mode == ConnectionViewMode.COMPACT,
            onClick = { onModeChange(ConnectionViewMode.COMPACT) },
        )
    }
}

@Composable
private fun ViewModeButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .size(Dimens.viewToggleButtonSize)
            .clip(RoundedCornerShape(Dimens.viewToggleCorner))
            .background(if (selected) Accent else ButtonOff)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = if (selected) TextOnAccent else TextDim,
            modifier = Modifier.size(Dimens.iconSizeMedium),
        )
    }
}
