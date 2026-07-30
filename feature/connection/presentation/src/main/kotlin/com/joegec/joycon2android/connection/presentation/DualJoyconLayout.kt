package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun DualJoyconLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.dualJoyconGap),
    ) {
        LeftJoyconVertical(state, onUnassign, Modifier.weight(1f))
        RightJoyconVertical(state, onUnassign, Modifier.weight(1f))
    }
}

@Composable
private fun LeftJoyconVertical(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pressed = state.pressed
    JoyconCard(
        accentColor = state.left?.accentColor,
        onClick = { onUnassign(state.left!!.address) },
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShoulderButton(JoyconButton.ZL.label, JoyconButton.ZL.id in pressed, Modifier.fillMaxWidth())
        ShoulderButton(JoyconButton.L.label, JoyconButton.L.id in pressed, Modifier.fillMaxWidth())
        MinusBatteryRow(state.leftInput, pressed)
        StickCard(state.leftStickX, state.leftStickY, JoyconButton.LS.id in pressed)
        DPad(pressed)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            CaptureButton(pressed)
        }
        LeftRailButtons(pressed)
        ImuDisplay(state.leftInput)
    }
}

@Composable
private fun RightJoyconVertical(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pressed = state.pressed
    JoyconCard(
        accentColor = state.right?.accentColor,
        onClick = { onUnassign(state.right!!.address) },
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShoulderButton(JoyconButton.ZR.label, JoyconButton.ZR.id in pressed, Modifier.fillMaxWidth())
        ShoulderButton(JoyconButton.R.label, JoyconButton.R.id in pressed, Modifier.fillMaxWidth())
        PlusBatteryRow(state.rightInput, pressed)
        FaceButtons(pressed)
        StickCard(state.rightStickX, state.rightStickY, JoyconButton.RS.id in pressed)
        HomeButtonRow(pressed)
        RightRailButtons(pressed)
        ImuDisplay(state.rightInput)
    }
}
