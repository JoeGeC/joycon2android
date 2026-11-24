package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RangeSlider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.TextTitle
import kotlin.math.roundToInt

/** Two thumbs that may share a value. Held locally while dragging; stored when let go. */
@Composable
fun PercentRangeSlider(
    title: String,
    low: Int,
    high: Int,
    range: IntRange,
    step: Int,
    valueLabel: @Composable (low: Int, high: Int) -> String,
    onSetPercents: (low: Int, high: Int) -> Unit,
    modifier: Modifier = Modifier,
    titleColor: Color = TextTitle,
) {
    var dragged by remember(low, high) { mutableStateOf(low.toFloat()..high.toFloat()) }
    val draggedLow = dragged.start.roundToInt()
    val draggedHigh = dragged.endInclusive.roundToInt()
    Column(modifier.fillMaxWidth()) {
        SliderHeader(title, valueLabel(draggedLow, draggedHigh), titleColor)
        RangeSlider(
            modifier = Modifier.reportsSliderTouch(LocalSliderTouch.current),
            value = dragged,
            onValueChange = { dragged = it },
            onValueChangeFinished = { onSetPercents(draggedLow, draggedHigh) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = (range.last - range.first) / step - 1,
            colors = settingSliderColors(),
        )
    }
}
