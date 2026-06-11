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
import com.joegec.joycon2android.ui.theme.JoyconDefaultColor
import com.joegec.joycon2android.ui.theme.joyconBorderColor

@Composable
internal fun RightSidewaysLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val borderColor = joyconBorderColor(state.right?.accentColor, JoyconDefaultColor)

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onUnassign(state.right!!.address) }
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, borderColor.copy(alpha = Dimens.cardBorderAlpha), shape)
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        RightRailButtons(state.pressed)
        MainBody(state)
        SidewaysImuDisplay(state.rightInput)
    }
}

@Composable
private fun MainBody(state: PlayerState) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        CenterContent(state, Modifier.weight(1f))
        Spacer(Modifier.width(Dimens.sidewaysContentGap))
        ShoulderColumn(state.pressed)
    }
}

@Composable
private fun ShoulderColumn(pressed: Set<String>) {
    Column(
        Modifier.fillMaxHeight().width(Dimens.sidewaysShoulderWidth),
        verticalArrangement = Arrangement.spacedBy(Dimens.sidewaysShoulderGap),
    ) {
        ShoulderButton(JoyconButton.R.label, JoyconButton.R.id in pressed, Modifier.fillMaxWidth().weight(1f))
        ShoulderButton(JoyconButton.ZR.label, JoyconButton.ZR.id in pressed, Modifier.fillMaxWidth().weight(1f))
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
        StickAndFaceButtons(state)
    }
}

@Composable
private fun TopRow(state: PlayerState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallButton(JoyconButton.Chat.label, JoyconButton.Chat.id in state.pressed)
            HomeButton(state.pressed)
        }
        AnimatedVisibility(
            visible = state.rightInput.batteryVolts > 0f,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BatteryPill(state.rightInput.batteryVolts)
        }
        SmallButton(JoyconButton.Plus.label, JoyconButton.Plus.id in state.pressed)
    }
}

@Composable
private fun StickAndFaceButtons(state: PlayerState) {
    val rotatedX = state.rightStickY
    val rotatedY = 4096 - state.rightStickX

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            StickCard(rotatedX, rotatedY, JoyconButton.RS.id in state.pressed,
                canvasSize = Dimens.sidewaysStickSize)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            FaceButtons(state.pressed, buttonSize = Dimens.sidewaysFaceSize, sideways = true)
        }
    }
}
