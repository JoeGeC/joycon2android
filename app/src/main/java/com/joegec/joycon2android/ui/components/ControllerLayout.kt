package com.joegec.joycon2android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.TextDim

private val LeftJoyconColor = Color(0xFF2D5BE3)
private val RightJoyconColor = Color(0xFFE33D2D)
private val ButtonOff = Color(0xFF1A1F26)

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
    Column(
        modifier
            .background(CardBg, RoundedCornerShape(20.dp))
            .border(2.dp, LeftJoyconColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ZL / L stacked full-width
        ShoulderButton("ZL", "ZL" in state.pressed, Modifier.fillMaxWidth())
        ShoulderButton("L", "L" in state.pressed, Modifier.fillMaxWidth())

        // Minus
        Box(Modifier.fillMaxWidth()) {
            SmallButton("-", "-" in state.pressed, Modifier.align(Alignment.CenterEnd))
        }

        // Left stick
        StickCard(state.leftStickX, state.leftStickY, "LS" in state.pressed)

        // D-pad
        DPad(state.pressed)

        // Capture
        Box(Modifier.fillMaxWidth()) {
            IconButton(
                on = "Camera" in state.pressed,
                modifier = Modifier.size(36.dp).align(Alignment.CenterEnd),
            ) {
                Icon(
                    Icons.Outlined.Circle,
                    contentDescription = "Capture",
                    modifier = Modifier.size(18.dp),
                    tint = if ("Camera" in state.pressed) Color(0xFF0E1116) else TextDim,
                )
            }
        }

        // SL / SR — each fill half width
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RailButton("SL", "SL(L)" in state.pressed, Modifier.weight(1f))
            RailButton("SR", "SR(L)" in state.pressed, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RightJoycon(state: ControllerState, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(CardBg, RoundedCornerShape(20.dp))
            .border(2.dp, RightJoyconColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ZR / R stacked full-width
        ShoulderButton("ZR", "ZR" in state.pressed, Modifier.fillMaxWidth())
        ShoulderButton("R", "R" in state.pressed, Modifier.fillMaxWidth())

        // Plus
        Box(Modifier.fillMaxWidth()) {
            SmallButton("+", "+" in state.pressed, Modifier.align(Alignment.CenterStart))
        }

        // Face buttons
        FaceButtons(state.pressed)

        // Right stick
        StickCard(state.rightStickX, state.rightStickY, "RS" in state.pressed)

        // Home and C
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                on = "Home" in state.pressed,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Filled.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(20.dp),
                    tint = if ("Home" in state.pressed) Color(0xFF0E1116) else TextDim,
                )
            }
            Spacer(Modifier.width(10.dp))
            SmallButton("C", "Chat" in state.pressed)
        }

        // SL / SR — each fill half width
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RailButton("SL", "SL(R)" in state.pressed, Modifier.weight(1f))
            RailButton("SR", "SR(R)" in state.pressed, Modifier.weight(1f))
        }
    }
}

@Composable
private fun RailButton(label: String, on: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(26.dp)
            .background(if (on) Accent else ButtonOff, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (on) Color(0xFF0E1116) else TextDim,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DPad(pressed: Set<String>, modifier: Modifier = Modifier) {
    val size = 46.dp
    Box(modifier.size(size * 3), contentAlignment = Alignment.Center) {
        DPadButton("▲", "Up" in pressed, Modifier.align(Alignment.TopCenter).size(size))
        DPadButton("▼", "Down" in pressed, Modifier.align(Alignment.BottomCenter).size(size))
        DPadButton("◀", "Left" in pressed, Modifier.align(Alignment.CenterStart).size(size))
        DPadButton("▶", "Right" in pressed, Modifier.align(Alignment.CenterEnd).size(size))
    }
}

@Composable
private fun DPadButton(symbol: String, on: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(CircleShape).background(if (on) Accent else ButtonOff),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = if (on) Color(0xFF0E1116) else TextDim, fontSize = 12.sp)
    }
}

@Composable
private fun FaceButtons(pressed: Set<String>, modifier: Modifier = Modifier) {
    val size = 46.dp
    Box(modifier.size(size * 3), contentAlignment = Alignment.Center) {
        FaceButton("Y", "Y" in pressed, Modifier.align(Alignment.CenterStart).offset(x = 4.dp))
        FaceButton("X", "X" in pressed, Modifier.align(Alignment.TopCenter).offset(y = 4.dp))
        FaceButton("A", "A" in pressed, Modifier.align(Alignment.CenterEnd).offset(x = (-4).dp))
        FaceButton("B", "B" in pressed, Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp))
    }
}

@Composable
private fun FaceButton(label: String, on: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier.size(46.dp).clip(CircleShape).background(if (on) Accent else ButtonOff),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (on) Color(0xFF0E1116) else Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ShoulderButton(label: String, on: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(30.dp)
            .background(if (on) Accent else ButtonOff, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (on) Color(0xFF0E1116) else TextDim,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun SmallButton(label: String, on: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(30.dp)
            .width(48.dp)
            .background(if (on) Accent else ButtonOff, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (on) Color(0xFF0E1116) else TextDim,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun IconButton(
    on: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier.clip(CircleShape).background(if (on) Accent else ButtonOff),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
