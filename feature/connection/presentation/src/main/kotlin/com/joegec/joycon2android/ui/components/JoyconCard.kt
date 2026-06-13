package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.JoyconDefaultColor
import com.joegec.joycon2android.ui.theme.joyconBorderColor

@Composable
internal fun JoyconCard(
    accentColor: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val borderColor = joyconBorderColor(accentColor, JoyconDefaultColor)

    Column(
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, borderColor.copy(alpha = Dimens.cardBorderAlpha), shape)
            .padding(Dimens.cardPadding),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
        content = content,
    )
}
