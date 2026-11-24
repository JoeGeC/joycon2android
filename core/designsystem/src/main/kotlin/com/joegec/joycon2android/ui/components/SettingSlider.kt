package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextTitle

@Composable
fun SettingSlider(
    title: String,
    description: String,
    valueLabel: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = TextTitle,
) {
    Column(modifier.fillMaxWidth()) {
        SliderHeader(title, valueLabel, titleColor)
        Spacer(Modifier.height(Dimens.featureCardTitleGap))
        Text(description, color = TextDim, style = MaterialTheme.typography.bodySmall)
        Slider(
            modifier = Modifier.reportsSliderTouch(LocalSliderTouch.current),
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
            colors = settingSliderColors(),
        )
    }
}
