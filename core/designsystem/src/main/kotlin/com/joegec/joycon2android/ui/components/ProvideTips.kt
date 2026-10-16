package com.joegec.joycon2android.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.core.designsystem.R

/** Tapping any tip asks whether to turn them all off. */
@Composable
fun ProvideTips(visible: Boolean, onTurnOff: () -> Unit, content: @Composable () -> Unit) {
    var asking by rememberSaveable { mutableStateOf(false) }
    val tips = remember(visible) { Tips(visible, onTap = { asking = true }) }

    CompositionLocalProvider(LocalTips provides tips) {
        content()
    }
    if (asking) {
        ConfirmDialog(
            title = stringResource(R.string.tips_turn_off_title),
            body = stringResource(R.string.tips_turn_off_body),
            confirmLabel = stringResource(R.string.tips_turn_off_confirm),
            dismissLabel = stringResource(R.string.tips_turn_off_dismiss),
            onConfirm = {
                asking = false
                onTurnOff()
            },
            onDismiss = { asking = false },
        )
    }
}
