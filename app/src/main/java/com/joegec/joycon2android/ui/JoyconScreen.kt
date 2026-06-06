package com.joegec.joycon2android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.ui.components.BatteryPill
import com.joegec.joycon2android.ui.components.ControllerLayout
import com.joegec.joycon2android.ui.components.ScanningIndicator
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun JoyconScreen(
    state: ControllerState,
    onConnect: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Header(state)
        if (state.anyConnected) {
            ConnectedContent(state, onStop)
        } else {
            DisconnectedContent(state, onConnect)
        }
    }
}

@Composable
private fun Header(state: ControllerState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "JOY-CON 2",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
            )
            val status = when {
                state.bothConnected -> "BOTH CONNECTED"
                state.left.connected -> "LEFT CONNECTED"
                state.right.connected -> "RIGHT CONNECTED"
                state.anyConnecting -> "CONNECTING…"
                state.scanning -> "SCANNING…"
                else -> "DISCONNECTED"
            }
            Text(
                status,
                color = if (state.anyConnected) Accent else TextDim,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        // Show battery for each connected side
        Column(horizontalAlignment = Alignment.End) {
            if (state.left.connected && state.leftInput.batteryVolts > 0f) {
                BatteryPill(state.leftInput.batteryVolts, "L")
            }
            if (state.right.connected && state.rightInput.batteryVolts > 0f) {
                BatteryPill(state.rightInput.batteryVolts, "R")
            }
        }
    }
}

@Composable
private fun DisconnectedContent(state: ControllerState, onConnect: () -> Unit) {
    val isBusy = state.scanning || state.anyConnecting

    Button(
        onClick = onConnect,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Accent),
        enabled = !isBusy,
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF0E1116),
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.size(10.dp))
        }
        Text(
            when {
                state.anyConnecting -> "Connecting…"
                state.scanning -> "Scanning…"
                else -> "Scan & Connect"
            },
            color = Color(0xFF0E1116),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }

    if (state.scanning) {
        ScanningIndicator()
    }

    // Show found devices
    if (state.left.connecting && state.left.deviceName != null) {
        Text(
            "Found: ${state.left.deviceName}",
            color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
        )
    }
    if (state.right.connecting && state.right.deviceName != null) {
        Text(
            "Found: ${state.right.deviceName}",
            color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
        )
    }

    if (state.error != null) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF2D1B1B), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(state.error, color = Color(0xFFFF6B6B), fontSize = 13.sp)
        }
    }

    if (!isBusy && state.error == null) {
        Text(
            "Press SYNC on your Joy-Con(s). Both left and right will be detected automatically.",
            color = TextDim,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ConnectedContent(state: ControllerState, onStop: () -> Unit) {
    ControllerLayout(state)

    // Per-side IMU and packet data
    if (state.left.connected) {
        Text(
            "L: Accel ${state.leftInput.accelX}, ${state.leftInput.accelY}, ${state.leftInput.accelZ}  " +
                "Gyro ${state.leftInput.gyroX}, ${state.leftInput.gyroY}, ${state.leftInput.gyroZ}  " +
                "pkt ${state.leftInput.packetId}",
            color = TextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
        )
    }
    if (state.right.connected) {
        Text(
            "R: Accel ${state.rightInput.accelX}, ${state.rightInput.accelY}, ${state.rightInput.accelZ}  " +
                "Gyro ${state.rightInput.gyroX}, ${state.rightInput.gyroY}, ${state.rightInput.gyroZ}  " +
                "pkt ${state.rightInput.packetId}",
            color = TextDim, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
        )
    }

    OutlinedButton(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(12.dp),
    ) { Text("Disconnect", color = TextDim) }
}
