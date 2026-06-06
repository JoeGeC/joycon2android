package com.joegec.joycon2android.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.LeftJoyconColor
import com.joegec.joycon2android.ui.theme.RightJoyconColor
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@Composable
internal fun PlayerControllerLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.hasFullController) {
        DualJoyconLayout(state, onUnassign, modifier)
    } else if (state.left != null) {
        LeftSidewaysLayout(state, onUnassign, modifier)
    } else if (state.right != null) {
        RightSidewaysLayout(state, onUnassign, modifier)
    }
}

// --- Dual joycon (vertical, side by side) ---

@Composable
private fun DualJoyconLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
    val input = state.leftInput
    val address = state.left!!.address

    Column(
        modifier
            .clip(shape)
            .clickable { onUnassign(address) }
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
private fun RightJoyconVertical(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val input = state.rightInput
    val address = state.right!!.address

    Column(
        modifier
            .clip(shape)
            .clickable { onUnassign(address) }
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

// --- Left joycon held sideways ---
// SL/SR top, ZL/L single column left (each half height), center: -/battery/capture, stick+dpad

@Composable
private fun LeftSidewaysLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val input = state.leftInput
    val address = state.left!!.address

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onUnassign(address) }
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, LeftJoyconColor.copy(alpha = 0.4f), shape)
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        // SL and SR along the top
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RailButton("SL", "SL(L)" in state.pressed, Modifier.weight(1f))
            RailButton("SR", "SR(L)" in state.pressed, Modifier.weight(1f))
        }

        // Main body: ZL/L column on the left, center content
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // ZL/L stacked vertically, each taking half
            Column(
                Modifier.fillMaxHeight().width(36.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ShoulderButton("ZL", "ZL" in state.pressed, Modifier.fillMaxWidth().weight(1f))
                ShoulderButton("L", "L" in state.pressed, Modifier.fillMaxWidth().weight(1f))
            }

            Spacer(Modifier.width(10.dp))

            // Center content
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
            ) {
                // - left, battery center, capture right
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SmallButton("-", "-" in state.pressed)
                    if (input.batteryVolts > 0f) {
                        BatteryPill(input.batteryVolts)
                    }
                    ControllerIconButton(
                        on = "Camera" in state.pressed,
                        modifier = Modifier.size(Dimens.iconButtonSize),
                    ) {
                        Icon(
                            Icons.Outlined.Circle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if ("Camera" in state.pressed) TextOnAccent else TextDim,
                        )
                    }
                }

                // Stick left, D-pad right — each takes equal space
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StickCard(state.leftStickX, state.leftStickY, "LS" in state.pressed,
                            canvasSize = Dimens.sidewaysStickSize)
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        DPad(state.pressed, buttonSize = Dimens.sidewaysDpadSize)
                    }
                }
            }
        }

        // IMU: accel and gyro on the same row
        SidewaysImuRow(input)

        // Packet ID
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ImuLabel(stringResource(R.string.imu_packet))
            ImuValue(input.packetId.toString())
        }
    }
}

// --- Right joycon held sideways ---
// SL/SR top, R/ZR single column right (each half height), center: home+chat/battery/+, stick+ABXY

@Composable
private fun RightSidewaysLayout(
    state: PlayerState,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val input = state.rightInput
    val address = state.right!!.address

    Column(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable { onUnassign(address) }
            .background(CardBg, shape)
            .border(Dimens.cardBorderWidth, RightJoyconColor.copy(alpha = 0.4f), shape)
            .padding(Dimens.cardPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        // SL and SR along the top
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RailButton("SL", "SL(R)" in state.pressed, Modifier.weight(1f))
            RailButton("SR", "SR(R)" in state.pressed, Modifier.weight(1f))
        }

        // Main body: center content, R/ZR column on the right
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Center content
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
            ) {
                // Chat/Home left, battery center, + right
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SmallButton("C", "Chat" in state.pressed)
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
                    }
                    if (input.batteryVolts > 0f) {
                        BatteryPill(input.batteryVolts)
                    }
                    SmallButton("+", "+" in state.pressed)
                }

                // Stick left, ABXY right — each takes equal space
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        StickCard(state.rightStickX, state.rightStickY, "RS" in state.pressed,
                            canvasSize = Dimens.sidewaysStickSize)
                    }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        FaceButtons(state.pressed, buttonSize = Dimens.sidewaysFaceSize)
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // R/ZR stacked vertically, each taking half
            Column(
                Modifier.fillMaxHeight().width(36.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ShoulderButton("R", "R" in state.pressed, Modifier.fillMaxWidth().weight(1f))
                ShoulderButton("ZR", "ZR" in state.pressed, Modifier.fillMaxWidth().weight(1f))
            }
        }

        // IMU: accel and gyro on the same row
        SidewaysImuRow(input)

        // Packet ID
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ImuLabel(stringResource(R.string.imu_packet))
            ImuValue(input.packetId.toString())
        }
    }
}

// --- Shared helpers ---

@Composable
private fun SidewaysImuRow(input: JoyconInput) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SidewaysImuColumn(stringResource(R.string.imu_accel), input.accelX, input.accelY, input.accelZ, Modifier.weight(1f))
        SidewaysImuColumn(stringResource(R.string.imu_gyro), input.gyroX, input.gyroY, input.gyroZ, Modifier.weight(1f))
    }
}

@Composable
private fun SidewaysImuColumn(title: String, x: Int, y: Int, z: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        ImuLabel(title)
        Spacer(Modifier.height(2.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ImuAxisValue("X", x)
            ImuAxisValue("Y", y)
            ImuAxisValue("Z", z)
        }
    }
}

@Composable
private fun ImuAxisValue(axis: String, value: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            axis,
            color = TextDim.copy(alpha = 0.6f),
            fontSize = Dimens.fontSizeLabel,
            fontFamily = FontFamily.Monospace,
        )
        Text(
            "%+6d".format(value),
            color = TextDim,
            fontSize = Dimens.fontSizeLabel,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun ImuLabel(text: String) {
    Text(
        text,
        color = TextDim.copy(alpha = 0.7f),
        fontSize = Dimens.fontSizeLabel,
        fontWeight = FontWeight.Bold,
        letterSpacing = Dimens.fontSizeLabel * 0.1f,
    )
}

@Composable
private fun ImuValue(text: String) {
    Text(
        text,
        color = TextDim,
        fontSize = Dimens.fontSizeLabel,
        fontFamily = FontFamily.Monospace,
    )
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
