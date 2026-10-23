package com.joegec.joycon2android.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.joegec.joycon2android.core.designsystem.R
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextBright
import com.joegec.joycon2android.ui.theme.TextDim

/** A filled dot while [lit], hollow otherwise, so the state never rests on colour alone. */
@Composable
fun StatusLight(label: String, lit: Boolean, modifier: Modifier = Modifier) {
    val state = stringResource(if (lit) R.string.status_light_on else R.string.status_light_off)
    val fill by animateColorAsState(if (lit) Accent else Color.Transparent, Motion.easeOut(), label = "fill")
    val ring by animateColorAsState(if (lit) Accent else TextDim, Motion.easeOut(), label = "ring")
    val text by animateColorAsState(if (lit) TextBright else TextDim, Motion.easeOut(), label = "text")
    Row(
        modifier.semantics { stateDescription = state },
        horizontalArrangement = Arrangement.spacedBy(Dimens.statusLightGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(Dimens.statusLightDot)
                .background(fill, CircleShape)
                .border(Dimens.cardBorderWidth / 2, ring, CircleShape),
        )
        Text(label, color = text, style = MaterialTheme.typography.bodySmall)
    }
}
