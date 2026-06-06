package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.LeftJoyconColor
import com.joegec.joycon2android.ui.theme.RightJoyconColor
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
internal fun PlayerControllerLayout(state: PlayerState, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (state.left != null) {
            LeftJoycon(state, Modifier.weight(1f))
        }
        if (state.right != null) {
            RightJoycon(state, Modifier.weight(1f))
        }
    }
}

@Composable
private fun LeftJoycon(state: PlayerState, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val input = state.leftInput

    Column(
        modifier
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, LeftJoyconColor.copy(alpha = 0.4f), shape)
            .padding(Dimens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        ShoulderButton("ZL", "ZL" in state.pressed, Modifier.fillMaxWidth())
        ShoulderButton("L", "L" in state.pressed, Modifier.fillMaxWidth())

        MinusButtonRow(input, state.pressed)
        StickCard(state.leftStickX, state.leftStickY, "LS" in state.pressed)
        DPad(state.pressed)
        CaptureButtonRow(state.pressed)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RailButton("SL", "SL(L)" in state.pressed, Modifier.weight(1f))
            RailButton("SR", "SR(L)" in state.pressed, Modifier.weight(1f))
        }

        ImuDisplay(input)
    }
}

@Composable
private fun RightJoycon(state: PlayerState, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val input = state.rightInput

    Column(
        modifier
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, RightJoyconColor.copy(alpha = 0.4f), shape)
            .padding(Dimens.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        ShoulderButton("ZR", "ZR" in state.pressed, Modifier.fillMaxWidth())
        ShoulderButton("R", "R" in state.pressed, Modifier.fillMaxWidth())

        PlusButtonRow(input, state.pressed)
        FaceButtons(state.pressed)
        StickCard(state.rightStickX, state.rightStickY, "RS" in state.pressed)
        HomeButtonRow(state.pressed)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RailButton("SL", "SL(R)" in state.pressed, Modifier.weight(1f))
            RailButton("SR", "SR(R)" in state.pressed, Modifier.weight(1f))
        }

        ImuDisplay(input)
    }
}

@Composable
private fun MinusButtonRow(input: JoyconInput, pressed: Set<String>) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (input.batteryVolts > 0f) {
            BatteryPill(input.batteryVolts)
        } else {
            Spacer(Modifier)
        }
        SmallButton("-", "-" in pressed)
    }
}

@Composable
private fun PlusButtonRow(input: JoyconInput, pressed: Set<String>) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallButton("+", "+" in pressed)
        if (input.batteryVolts > 0f) {
            BatteryPill(input.batteryVolts)
        } else {
            Spacer(Modifier)
        }
    }
}

@Composable
private fun CaptureButtonRow(pressed: Set<String>) {
    Box(Modifier.fillMaxWidth()) {
        ControllerIconButton(
            on = "Camera" in pressed,
            modifier = Modifier.size(Dimens.iconButtonSize).align(Alignment.CenterEnd),
        ) {
            Icon(
                Icons.Outlined.Circle,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if ("Camera" in pressed) TextOnAccent else TextDim,
            )
        }
    }
}

@Composable
private fun HomeButtonRow(pressed: Set<String>) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControllerIconButton(
            on = "Home" in pressed,
            modifier = Modifier.size(Dimens.iconButtonSize),
        ) {
            Icon(
                Icons.Filled.Home,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if ("Home" in pressed) TextOnAccent else TextDim,
            )
        }
        Spacer(Modifier.width(10.dp))
        SmallButton("C", "Chat" in pressed)
    }
}
