package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun SliderHeader(title: String, valueLabel: String, titleColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = titleColor, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(Dimens.featureCardSwitchGap))
        Text(valueLabel, color = Accent, style = MaterialTheme.typography.bodyMedium)
    }
}
