package com.joegec.joycon2android.connection.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.model.GameCubeButtons
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.Dimens

@Composable
internal fun ProControllerLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pressed = state.pressed
    val buttons = if (state.hasGameCube) GameCubeButtons.present else JoyconButton.entries.toSet()
    val marking = { button: JoyconButton ->
        (if (state.hasGameCube) GameCubeButtons.marking(button) else null) ?: button.label
    }
    JoyconCard(
        controller = state.left,
        onClick = { onUnassign(state.left!!.address) },
        modifier = modifier.fillMaxWidth(),
    ) {
        ShoulderButtons(pressed, buttons, marking)
        ButtonRow(state, pressed, buttons)
        SticksRow(state, pressed, buttons)
        SpecialButtons(pressed)
        SidewaysImuDisplay(state.leftInput)
    }
}

@Composable
private fun ShoulderButtons(pressed: Set<String>, buttons: Set<JoyconButton>, marking: (JoyconButton) -> String) {
    Row(
        Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.sidewaysShoulderGap),
        ) {
            listOf(JoyconButton.ZL, JoyconButton.L, JoyconButton.GL).filter { it in buttons }.forEach {
                ShoulderButton(marking(it), it.id in pressed, Modifier.fillMaxWidth())
            }
        }
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimens.sidewaysShoulderGap),
        ) {
            listOf(JoyconButton.ZR, JoyconButton.R, JoyconButton.GR).filter { it in buttons }.forEach {
                ShoulderButton(marking(it), it.id in pressed, Modifier.fillMaxWidth())
            }
        }
    }
}

// The ends are weighted so the battery stays centred when − is missing.
@Composable
private fun ButtonRow(state: PlayerState, pressed: Set<String>, buttons: Set<JoyconButton>) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (JoyconButton.Minus in buttons) SmallButton(JoyconButton.Minus.label, JoyconButton.Minus.id in pressed)
        }
        state.leftInput.battery?.let { BatteryPill(it) }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            SmallButton(JoyconButton.Plus.label, JoyconButton.Plus.id in pressed)
        }
    }
}

@Composable
private fun SticksRow(state: PlayerState, pressed: Set<String>, buttons: Set<JoyconButton>) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            StickCard(state.leftStickX, state.leftStickY, JoyconButton.LS in buttons && JoyconButton.LS.id in pressed,
                canvasSize = Dimens.sidewaysStickSize)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        ) {
            DPad(pressed, buttonSize = Dimens.sidewaysDpadSize)
            FaceButtons(pressed, buttonSize = Dimens.sidewaysFaceSize)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            StickCard(state.rightStickX, state.rightStickY, JoyconButton.RS in buttons && JoyconButton.RS.id in pressed,
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
