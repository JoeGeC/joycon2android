package com.joegec.joycon2android.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.Motion
import com.joegec.joycon2android.ui.theme.TextDim

/** [status] lights in the accent while [lit], which can differ from [checked]: a device up for another reason. */
@Composable
fun ToggleRow(
    status: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    lit: Boolean = checked,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        val statusColor by animateColorAsState(if (lit) Accent else TextDim, Motion.easeOut(), label = "status")
        AnimatedContent(
            targetState = status,
            transitionSpec = { fadeIn(Motion.easeOut()) togetherWith fadeOut(Motion.easeOut()) },
            label = "status",
            modifier = Modifier.weight(1f),
        ) { text ->
            Text(text, color = statusColor, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.width(Dimens.featureCardSwitchGap))
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Accent),
        )
    }
}
