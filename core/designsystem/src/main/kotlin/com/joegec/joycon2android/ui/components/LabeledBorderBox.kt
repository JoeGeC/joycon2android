package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.Dimens

/**
 * Fieldset-style container: a rounded border with [label] straddling the top edge, its own
 * background masking the border line behind it — like an HTML `<legend>` or an outlined text
 * field's notched label. [labelBackground] must match the surface the box sits on for the mask
 * to read as a clean gap.
 */
@Composable
fun LabeledBorderBox(
    label: String,
    borderColor: Color,
    labelBackground: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier.padding(top = Dimens.legendOverhang)) {
        Box(
            Modifier
                .fillMaxWidth()
                .border(Dimens.cardBorderWidth, borderColor, RoundedCornerShape(Dimens.buttonCorner))
                .padding(horizontal = Dimens.cardPadding, vertical = Dimens.legendContentVertical),
        ) {
            content()
        }
        Text(
            label,
            color = borderColor,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = Dimens.legendInset, y = -Dimens.legendOverhang)
                .background(labelBackground)
                .padding(horizontal = Dimens.legendLabelPadding),
        )
    }
}
