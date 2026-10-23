package com.joegec.joycon2android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.AccentDim
import com.joegec.joycon2android.ui.theme.ButtonOff
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextTitle

/** One of a few options, all in view, the highlight sliding to the choice: output mode, Auto / On / Off. */
@Composable
fun <T> SegmentedChoice(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.buttonCorner)
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(Dimens.minTouchTarget)
            .background(ButtonOff, shape),
    ) {
        val segment = maxWidth / options.size
        val index = options.indexOfFirst { it.first == selected }.coerceAtLeast(0)
        val indicatorOffset by animateDpAsState(segment * index, Motion.easeOut(), label = "segment")
        Box(
            Modifier
                .offset(x = indicatorOffset)
                .width(segment)
                .fillMaxHeight()
                .background(AccentDim, shape)
                .border(Dimens.cardBorderWidth / 2, Accent, shape),
        )
        Row(Modifier.fillMaxWidth().selectableGroup()) {
            options.forEach { (value, label) ->
                val chosen = value == selected
                val color by animateColorAsState(if (chosen) TextTitle else TextDim, Motion.easeOut(), label = "label")
                Box(
                    Modifier
                        .width(segment)
                        .fillMaxHeight()
                        .selectable(selected = chosen, role = Role.RadioButton, onClick = { onSelect(value) }),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = color,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = Dimens.buttonHorizontalPadding),
                    )
                }
            }
        }
    }
}
