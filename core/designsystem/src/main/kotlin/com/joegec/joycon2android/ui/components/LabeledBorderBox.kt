package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.Dimens

/** Fieldset-style box with [label] notched into the top border; [labelBackground] must match the surface behind. */
@Composable
fun LabeledBorderBox(
    label: String,
    borderColor: Color,
    labelBackground: Color,
    modifier: Modifier = Modifier,
    onInfoClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(modifier.padding(top = Dimens.legendOverhang)) {
        Box(
            Modifier
                .fillMaxWidth()
                .border(Dimens.cardBorderWidth, borderColor, RoundedCornerShape(Dimens.buttonCorner))
                .padding(horizontal = Dimens.cardPadding, vertical = Dimens.legendContentVertical),
        ) {
            content()
        }
        Legend(
            label = label,
            color = borderColor,
            background = labelBackground,
            onInfoClick = onInfoClick,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = Dimens.legendInset, y = -Dimens.legendOverhang),
        )
    }
}

@Composable
private fun Legend(
    label: String,
    color: Color,
    background: Color,
    onInfoClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .background(background)
            .let { if (onInfoClick != null) it.clickable(onClick = onInfoClick) else it }
            .padding(horizontal = Dimens.legendLabelPadding),
        horizontalArrangement = Arrangement.spacedBy(Dimens.legendLabelPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = color, style = MaterialTheme.typography.labelLarge)
        if (onInfoClick != null) {
            Icon(
                Icons.Outlined.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Dimens.iconSizeSmall),
            )
        }
    }
}
