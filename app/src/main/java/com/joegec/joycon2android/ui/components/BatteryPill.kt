package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.joegec.joycon2android.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.AccentDim
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun BatteryPill(volts: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(AccentDim, RoundedCornerShape(Dimens.pillCorner))
            .padding(horizontal = Dimens.pillPaddingHorizontal, vertical = Dimens.pillPaddingVertical)
    ) {
        Text(
            stringResource(R.string.battery_format, volts),
            color = Accent,
            fontSize = Dimens.fontSizeBattery,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}
