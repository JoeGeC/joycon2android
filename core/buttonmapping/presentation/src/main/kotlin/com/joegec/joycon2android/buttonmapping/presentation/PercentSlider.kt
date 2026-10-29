package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.ui.components.SettingSlider
import com.joegec.joycon2android.ui.theme.Dimens
import kotlin.math.roundToInt

/** Held locally while dragging, so the thumb doesn't wait on storage; stored when let go. */
@Composable
internal fun PercentSlider(
    title: String,
    description: String,
    percent: Int,
    range: IntRange,
    step: Int,
    valueLabel: @Composable (Int) -> String,
    onSetPercent: (Int) -> Unit,
) {
    var dragged by remember(percent) { mutableFloatStateOf(percent.toFloat()) }
    SettingSlider(
        title = title,
        description = description,
        valueLabel = valueLabel(dragged.roundToInt()),
        value = dragged,
        valueRange = range.first.toFloat()..range.last.toFloat(),
        steps = (range.last - range.first) / step - 1,
        onValueChange = { dragged = it },
        onValueChangeFinished = { onSetPercent(dragged.roundToInt()) },
        modifier = Modifier.padding(bottom = Dimens.mappingSwitchBottomPadding),
    )
}
