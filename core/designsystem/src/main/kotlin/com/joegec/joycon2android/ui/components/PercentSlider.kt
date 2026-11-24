package com.joegec.joycon2android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.TextTitle
import kotlin.math.roundToInt

/** Held locally while dragging, so the thumb doesn't wait on storage; stored when let go. */
@Composable
fun PercentSlider(
    title: String,
    description: String,
    percent: Int,
    range: IntRange,
    step: Int,
    valueLabel: @Composable (Int) -> String,
    onSetPercent: (Int) -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = TextTitle,
) {
    var dragged by remember(percent) { mutableFloatStateOf(percent.toFloat()) }
    SettingSlider(
        title = title,
        titleColor = titleColor,
        description = description,
        valueLabel = valueLabel(dragged.roundToInt()),
        value = dragged,
        valueRange = range.first.toFloat()..range.last.toFloat(),
        steps = (range.last - range.first) / step - 1,
        onValueChange = { dragged = it },
        onValueChangeFinished = { onSetPercent(dragged.roundToInt()) },
        modifier = modifier,
    )
}
