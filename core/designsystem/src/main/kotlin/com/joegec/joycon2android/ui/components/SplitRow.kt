package com.joegec.joycon2android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

/**
 * Two children side by side: at [split] 1 [start] takes what [end] leaves at its natural width, at 0
 * [end] takes the whole row. In between both widths ease, which weights in a Row can't.
 */
@Composable
internal fun SplitRow(
    split: Float,
    gap: Dp,
    start: @Composable () -> Unit,
    end: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(contents = listOf(start, end), modifier = modifier) { (startSlot, endSlot), constraints ->
        val total = constraints.maxWidth
        val endMeasurable = endSlot.single()
        val natural = endMeasurable.maxIntrinsicWidth(constraints.maxHeight).coerceAtMost(total)
        val endWidth = (natural + (total - natural) * (1 - split)).roundToInt()
        val gapPx = (gap.roundToPx() * split).roundToInt()
        val startWidth = (total - endWidth - gapPx).coerceAtLeast(0)

        val endPlaced = endMeasurable.measure(Constraints.fixedWidth(endWidth).copy(maxHeight = constraints.maxHeight))
        val startPlaced = startSlot.map { it.measure(Constraints.fixedWidth(startWidth).copy(maxHeight = constraints.maxHeight)) }
        val height = maxOf(endPlaced.height, startPlaced.maxOfOrNull { it.height } ?: 0)
        layout(total, height) {
            startPlaced.forEach { it.place(0, (height - it.height) / 2) }
            endPlaced.place(startWidth + gapPx, (height - endPlaced.height) / 2)
        }
    }
}
