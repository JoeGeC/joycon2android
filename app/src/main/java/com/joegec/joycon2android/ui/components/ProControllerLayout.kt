package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun ProControllerLayout(
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
            .border(Dimens.cardBorderWidth, Color.White.copy(alpha = Dimens.cardBorderAlpha), shape)
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        ShoulderButtons(state.pressed)
        ButtonRow(state)
        SticksRow(state)
        SpecialButtons(state.pressed)
        SidewaysImuDisplay(state.leftInput)
    }
}

@Composable
private fun ShoulderButtons(pressed: Set<String>) {
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.sidewaysShoulderGap),
        ) {
            ShoulderButton(JoyconButton.ZL.label, JoyconButton.ZL.id in pressed, Modifier.fillMaxWidth())
            ShoulderButton(JoyconButton.L.label, JoyconButton.L.id in pressed, Modifier.fillMaxWidth())
        }
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.sidewaysShoulderGap),
        ) {
            ShoulderButton(JoyconButton.ZR.label, JoyconButton.ZR.id in pressed, Modifier.fillMaxWidth())
            ShoulderButton(JoyconButton.R.label, JoyconButton.R.id in pressed, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ButtonRow(state: PlayerState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallButton(JoyconButton.Minus.label, JoyconButton.Minus.id in state.pressed)
        BatteryPill(state.leftInput.batteryVolts)
        SmallButton(JoyconButton.Plus.label, JoyconButton.Plus.id in state.pressed)
    }
}

@Composable
private fun SticksRow(state: PlayerState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            StickCard(state.leftStickX, state.leftStickY, JoyconButton.LS.id in state.pressed,
                canvasSize = Dimens.sidewaysStickSize)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        ) {
            DPad(state.pressed, buttonSize = Dimens.sidewaysDpadSize)
            FaceButtons(state.pressed, buttonSize = Dimens.sidewaysFaceSize)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            StickCard(state.rightStickX, state.rightStickY, JoyconButton.RS.id in state.pressed,
                canvasSize = Dimens.sidewaysStickSize)
        }
    }
}

@Composable
private fun SpecialButtons(pressed: Set<String>) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CaptureButton(pressed)
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeButton(pressed)
            SmallButton(JoyconButton.Chat.label, JoyconButton.Chat.id in pressed)
        }
    }
}
