package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import com.joegec.joycon2android.ui.components.HintBubble
import com.joegec.joycon2android.ui.components.LocalTips

/** Sits under a [LayoutRow], its pointer on the row's save button. */
@Composable
fun ColumnScope.SaveLayoutHint(visible: Boolean, text: String) {
    val tips = LocalTips.current
    AnimatedVisibility(
        visible = visible && tips.visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        HintBubble(
            text,
            pointerCenterFromEnd = LocalMinimumInteractiveComponentSize.current / 2,
            onClick = tips.onTap,
        )
    }
}
