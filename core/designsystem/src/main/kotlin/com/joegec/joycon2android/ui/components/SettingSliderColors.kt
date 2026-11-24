package com.joegec.joycon2android.ui.components

import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.AccentDim

@Composable
internal fun settingSliderColors(): SliderColors = SliderDefaults.colors(
    thumbColor = Color.White,
    activeTrackColor = Accent,
    inactiveTrackColor = AccentDim,
    activeTickColor = Color.Transparent,
    inactiveTickColor = Color.Transparent,
)
