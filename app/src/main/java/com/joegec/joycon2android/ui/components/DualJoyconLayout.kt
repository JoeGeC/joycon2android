package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.JoyconDefaultColor
import com.joegec.joycon2android.ui.theme.joyconBorderColor

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
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val borderColor = joyconBorderColor(state.left?.accentColor, JoyconDefaultColor)

    Column(
        modifier
            .clip(shape)
            .clickable { onUnassign(state.left!!.address) }
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, borderColor.copy(alpha = Dimens.cardBorderAlpha), shape)
            .padding(Dimens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        ShoulderButton(JoyconButton.ZL.label, JoyconButton.ZL.id in state.pressed, Modifier.fillMaxWidth())
        ShoulderButton(JoyconButton.L.label, JoyconButton.L.id in state.pressed, Modifier.fillMaxWidth())
        MinusBatteryRow(state.leftInput, state.pressed)
        StickCard(state.leftStickX, state.leftStickY, JoyconButton.LS.id in state.pressed)
        DPad(state.pressed)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            CaptureButton(state.pressed)
        }
        LeftRailButtons(state.pressed)
        ImuDisplay(state.leftInput)
    }
}

@Composable
private fun RightJoyconVertical(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val borderColor = joyconBorderColor(state.right?.accentColor, JoyconDefaultColor)

    Column(
        modifier
            .clip(shape)
            .clickable { onUnassign(state.right!!.address) }
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, borderColor.copy(alpha = Dimens.cardBorderAlpha), shape)
            .padding(Dimens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        ShoulderButton(JoyconButton.ZR.label, JoyconButton.ZR.id in state.pressed, Modifier.fillMaxWidth())
        ShoulderButton(JoyconButton.R.label, JoyconButton.R.id in state.pressed, Modifier.fillMaxWidth())
        PlusBatteryRow(state.rightInput, state.pressed)
        FaceButtons(state.pressed)
        StickCard(state.rightStickX, state.rightStickY, JoyconButton.RS.id in state.pressed)
        HomeButtonRow(state.pressed)
        RightRailButtons(state.pressed)
        ImuDisplay(state.rightInput)
    }
}
