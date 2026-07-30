package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.connection.presentation.R
import com.joegec.joycon2android.model.ConnectionViewMode
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
            .height(Dimens.minTouchTarget)
            .clip(RoundedCornerShape(Dimens.buttonCorner))
            .background(ButtonOff)
            .clickable {
                onModeChange(
                    if (mode == ConnectionViewMode.DETAILED) ConnectionViewMode.COMPACT
                    else ConnectionViewMode.DETAILED
                )
            }
            .padding(Dimens.viewTogglePadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.viewTogglePadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ViewModeSegment(
            icon = Icons.Filled.ViewAgenda,
            contentDescription = stringResource(R.string.view_detailed),
            selected = mode == ConnectionViewMode.DETAILED,
        )
        ViewModeSegment(
            icon = Icons.AutoMirrored.Filled.ViewList,
            contentDescription = stringResource(R.string.view_compact),
            selected = mode == ConnectionViewMode.COMPACT,
        )
    }
}

@Composable
private fun ViewModeSegment(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
) {
    Box(
        Modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(Dimens.viewToggleCorner))
            .background(if (selected) Accent else Color.Transparent),
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
