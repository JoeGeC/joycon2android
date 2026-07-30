package com.joegec.joycon2android.connection.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun MinusBatteryRow(input: JoyconInput, pressed: Set<String>, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AnimatedVisibility(
            visible = input.batteryVolts > 0f,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BatteryPill(input.batteryVolts)
        }
        SmallButton(JoyconButton.Minus.label, JoyconButton.Minus.id in pressed)
    }
}

@Composable
internal fun PlusBatteryRow(input: JoyconInput, pressed: Set<String>, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SmallButton(JoyconButton.Plus.label, JoyconButton.Plus.id in pressed)
        AnimatedVisibility(
            visible = input.batteryVolts > 0f,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            BatteryPill(input.batteryVolts)
        }
    }
}

@Composable
internal fun CaptureButton(pressed: Set<String>, modifier: Modifier = Modifier) {
    val on = JoyconButton.Camera.id in pressed
    val accent = LocalControllerAccent.current
    ControllerIconButton(
        on = on,
        modifier = modifier.size(Dimens.iconButtonSize),
    ) {
        Icon(
            Icons.Outlined.Circle,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeSmall),
            tint = if (on) accent.onColor else Color.White,
        )
    }
}

@Composable
internal fun HomeButton(pressed: Set<String>, modifier: Modifier = Modifier) {
    val on = JoyconButton.Home.id in pressed
    val accent = LocalControllerAccent.current
    ControllerIconButton(
        on = on,
        modifier = modifier.size(Dimens.iconButtonSize),
    ) {
        Icon(
            Icons.Filled.Home,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconSizeMedium),
            tint = if (on) accent.onColor else Color.White,
        )
    }
}

@Composable
internal fun HomeButtonRow(pressed: Set<String>, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HomeButton(pressed)
        Spacer(Modifier.width(Dimens.sidewaysContentGap))
        SmallButton(JoyconButton.Chat.label, JoyconButton.Chat.id in pressed)
    }
}

@Composable
internal fun LeftRailButtons(pressed: Set<String>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        RailButton(JoyconButton.SlLeft.label, JoyconButton.SlLeft.id in pressed, Modifier.weight(1f))
        RailButton(JoyconButton.SrLeft.label, JoyconButton.SrLeft.id in pressed, Modifier.weight(1f))
    }
}

@Composable
internal fun RightRailButtons(pressed: Set<String>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)) {
        RailButton(JoyconButton.SlRight.label, JoyconButton.SlRight.id in pressed, Modifier.weight(1f))
        RailButton(JoyconButton.SrRight.label, JoyconButton.SrRight.id in pressed, Modifier.weight(1f))
    }
}
