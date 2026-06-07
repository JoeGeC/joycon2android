package com.joegec.joycon2android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.LeftJoyconColor

@Composable
internal fun LeftSidewaysLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.cardCorner)

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onUnassign(state.left!!.address) }
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, LeftJoyconColor.copy(alpha = Dimens.cardBorderAlpha), shape)
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        LeftRailButtons(state.pressed)
        MainBody(state)
        SidewaysImuDisplay(state.leftInput)
    }
}

@Composable
private fun MainBody(state: PlayerState) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        ShoulderColumn(state.pressed)
        Spacer(Modifier.width(Dimens.sidewaysContentGap))
        CenterContent(state, Modifier.weight(1f))
    }
}

@Composable
private fun ShoulderColumn(pressed: Set<String>) {
    Column(
        Modifier.fillMaxHeight().width(Dimens.sidewaysShoulderWidth),
        verticalArrangement = Arrangement.spacedBy(Dimens.sidewaysShoulderGap),
    ) {
        ShoulderButton(JoyconButton.ZL.label, JoyconButton.ZL.id in pressed, Modifier.fillMaxWidth().weight(1f))
        ShoulderButton(JoyconButton.L.label, JoyconButton.L.id in pressed, Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun CenterContent(state: PlayerState, modifier: Modifier = Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        TopRow(state)
        StickAndDpad(state)
    }
}

@Composable
private fun TopRow(state: PlayerState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallButton(JoyconButton.Minus.label, JoyconButton.Minus.id in state.pressed)
        AnimatedVisibility(
            visible = state.leftInput.batteryVolts > 0f,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BatteryPill(state.leftInput.batteryVolts)
        }
        CaptureButton(state.pressed)
    }
}

@Composable
private fun StickAndDpad(state: PlayerState) {
    val rotatedX = 4096 - state.leftStickY
    val rotatedY = state.leftStickX

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            StickCard(rotatedX, rotatedY, JoyconButton.LS.id in state.pressed,
                canvasSize = Dimens.sidewaysStickSize)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            DPad(state.pressed, buttonSize = Dimens.sidewaysDpadSize, sideways = true)
        }
    }
}
