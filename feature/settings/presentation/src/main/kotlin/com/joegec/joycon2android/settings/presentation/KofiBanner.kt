package com.joegec.joycon2android.settings.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.JoyconBlue
import com.joegec.joycon2android.ui.theme.JoyconRed
import com.joegec.joycon2android.ui.theme.TextDim

private const val KofiUrl = "https://ko-fi.com/joestechprojects"

@Composable
internal fun KofiBanner(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val gradientBorder = Brush.linearGradient(listOf(JoyconBlue, JoyconRed))

    Row(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .border(Dimens.cardBorderWidth, gradientBorder, shape)
            .background(CardBg)
            .clickable { uriHandler.openUri(KofiUrl) }
            .padding(Dimens.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.kofi),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Text(
            stringResource(R.string.kofi_banner),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextDim,
        )
    }
}
