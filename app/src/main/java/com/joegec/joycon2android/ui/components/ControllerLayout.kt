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
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.LeftJoyconColor
import com.joegec.joycon2android.ui.theme.RightJoyconColor
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
internal fun ControllerLayout(state: ControllerState, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LeftJoycon(state, Modifier.weight(1f))
        RightJoycon(state, Modifier.weight(1f))
    }
}

@Composable
private fun LeftJoycon(state: ControllerState, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.cardCorner)

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

        MinusButtonRow(state)
        StickCard(state.leftStickX, state.leftStickY, "LS" in state.pressed)
        DPad(state.pressed)
        CaptureButtonRow(state)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RailButton("SL", "SL(L)" in state.pressed, Modifier.weight(1f))
            RailButton("SR", "SR(L)" in state.pressed, Modifier.weight(1f))
        }

        if (state.left.connected) {
            ImuDisplay(state.leftInput)
        }
    }
}

@Composable
private fun RightJoycon(state: ControllerState, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(Dimens.cardCorner)

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

        PlusButtonRow(state)
        FaceButtons(state.pressed)
        StickCard(state.rightStickX, state.rightStickY, "RS" in state.pressed)
        HomeButtonRow(state)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RailButton("SL", "SL(R)" in state.pressed, Modifier.weight(1f))
            RailButton("SR", "SR(R)" in state.pressed, Modifier.weight(1f))
        }

        if (state.right.connected) {
            ImuDisplay(state.rightInput)
        }
    }
}

@Composable
private fun MinusButtonRow(state: ControllerState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.left.connected && state.leftInput.batteryVolts > 0f) {
            BatteryPill(state.leftInput.batteryVolts)
        } else {
            Spacer(Modifier)
        }
        SmallButton("-", "-" in state.pressed)
    }
}

@Composable
private fun PlusButtonRow(state: ControllerState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallButton("+", "+" in state.pressed)
        if (state.right.connected && state.rightInput.batteryVolts > 0f) {
            BatteryPill(state.rightInput.batteryVolts)
        } else {
            Spacer(Modifier)
        }
    }
}

@Composable
private fun CaptureButtonRow(state: ControllerState) {
    Box(Modifier.fillMaxWidth()) {
        ControllerIconButton(
            on = "Camera" in state.pressed,
            modifier = Modifier.size(Dimens.iconButtonSize).align(Alignment.CenterEnd),
        ) {
            Icon(
                Icons.Outlined.Circle,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if ("Camera" in state.pressed) TextOnAccent else TextDim,
            )
        }
    }
}

@Composable
private fun HomeButtonRow(state: ControllerState) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControllerIconButton(
            on = "Home" in state.pressed,
            modifier = Modifier.size(Dimens.iconButtonSize),
        ) {
            Icon(
                Icons.Filled.Home,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if ("Home" in state.pressed) TextOnAccent else TextDim,
            )
        }
        Spacer(Modifier.width(10.dp))
        SmallButton("C", "Chat" in state.pressed)
    }
}
