package com.joegec.joycon2android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Minimal sample app: scan, connect to one Joy-Con 2, show live input.
 *
 * AndroidManifest needs (API 31+):
 *   BLUETOOTH_SCAN, BLUETOOTH_CONNECT
 * and for older APIs: BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION.
 */
class MainActivity : ComponentActivity() {

    private lateinit var manager: Joycon2Manager

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val stateHolder = mutableStateOf(Joycon2State())
        manager = Joycon2Manager(this) { s -> runOnUiThread { stateHolder.value = s } }

        val permLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            if (grants.values.all { it }) {
                manager.startScan()
            } else {
                stateHolder.value = Joycon2State(error = "Bluetooth permissions denied")
            }
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    Modifier.fillMaxSize(),
                    color = Color(0xFF0E1116),
                ) {
                    JoyconScreen(
                        state = stateHolder.value,
                        onConnect = { permLauncher.launch(requiredPermissions()) },
                        onStop = { manager.stop() },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.stop()
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
}

private val accent = Color(0xFF38E0C8)
private val accentDim = Color(0xFF1C3A38)
private val cardBg = Color(0xFF161B22)
private val textDim = Color(0xFF8B98A5)

@Composable
fun JoyconScreen(state: Joycon2State, onConnect: () -> Unit, onStop: () -> Unit) {
    Column(
        Modifier.fillMaxSize().systemBarsPadding().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "JOY-CON 2",
                    color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                )
                val status = when {
                    state.connected -> "CONNECTED · ${state.side}"
                    state.connecting -> "CONNECTING…"
                    state.scanning -> "SCANNING…"
                    else -> "DISCONNECTED"
                }
                Text(
                    status, color = if (state.connected) accent else textDim,
                    fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Medium,
                )
            }
            if (state.connected) {
                BatteryPill(state.batteryVolts)
            }
        }

        // Connect / stop
        if (!state.connected) {
            val isBusy = state.scanning || state.connecting
            Button(
                onClick = onConnect,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent),
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
                        state.connecting -> "Connecting…"
                        state.scanning -> "Scanning…"
                        else -> "Scan & Connect"
                    },
                    color = Color(0xFF0E1116), fontWeight = FontWeight.Bold, fontSize = 16.sp,
                )
            }

            if (state.scanning) {
                ScanningIndicator()
            }

            if (state.foundDeviceName != null && state.connecting) {
                Text(
                    "Found: ${state.foundDeviceName}",
                    color = accent, fontSize = 13.sp, fontWeight = FontWeight.Medium,
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
                    "Press the Joy-Con SYNC button first. If it won't connect after a few tries, wait a minute (connect cooldown).",
                    color = textDim, fontSize = 12.sp,
                )
            }
        } else {
            OutlinedButton(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
            ) { Text("Disconnect", color = textDim) }
        }

        if (state.connected) {
            // Sticks
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StickCard("LEFT STICK", state.leftStickX, state.leftStickY, Modifier.weight(1f))
                StickCard("RIGHT STICK", state.rightStickX, state.rightStickY, Modifier.weight(1f))
            }
            // Triggers
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TriggerBar("ZL/L", state.triggerL, Modifier.weight(1f))
                TriggerBar("ZR/R", state.triggerR, Modifier.weight(1f))
            }
            // Buttons
            ButtonsCard(state.pressed)
            // Motion (small, for confirming IMU stream)
            Text(
                "Accel ${state.accelX}, ${state.accelY}, ${state.accelZ}   " +
                        "Gyro ${state.gyroX}, ${state.gyroY}, ${state.gyroZ}",
                color = textDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace,
            )
            Text(
                "pkt ${state.packetId}",
                color = Color(0xFF44505C), fontSize = 10.sp, fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun ScanningIndicator() {
    val transition = rememberInfiniteTransition(label = "scan")
    val dots by transition.animateFloat(
        initialValue = 0f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Restart),
        label = "dots",
    )
    Box(
        Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Text(
                "Looking for Joy-Con 2" + ".".repeat(dots.toInt() + 1),
                color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Make sure the controller is in pairing mode (SYNC button held)",
                color = textDim, fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun BatteryPill(volts: Float) {
    Box(
        Modifier
            .background(accentDim, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            "%.2f V".format(volts),
            color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun StickCard(label: String, x: Int, y: Int, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(cardBg, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = textDim, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        // 12-bit stick: 0..4095, center ~2048. Normalize to -1..1.
        val nx = (x - 2048f) / 2048f
        val ny = (y - 2048f) / 2048f
        Canvas(Modifier.size(120.dp)) {
            val r = size.minDimension / 2f
            val c = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = Color(0xFF0E1116), radius = r, center = c)
            drawCircle(color = accentDim, radius = r, center = c, style = Stroke(2f))
            // crosshair
            drawLine(Color(0xFF222C36), Offset(c.x - r, c.y), Offset(c.x + r, c.y), 1f)
            drawLine(Color(0xFF222C36), Offset(c.x, c.y - r), Offset(c.x, c.y + r), 1f)
            // dot (invert Y so up = up)
            val dot = Offset(c.x + nx * r * 0.85f, c.y + ny * r * 0.85f)
            drawCircle(color = accent, radius = 10f, center = dot)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "$x, $y", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun TriggerBar(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(
        modifier.background(cardBg, RoundedCornerShape(16.dp)).padding(14.dp)
    ) {
        Text(label, color = textDim, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(8.dp))
        val frac = (value / 255f).coerceIn(0f, 1f)
        Box(
            Modifier.fillMaxWidth().height(14.dp)
                .background(Color(0xFF0E1116), RoundedCornerShape(7.dp))
        ) {
            Box(
                Modifier.fillMaxWidth(frac).height(14.dp)
                    .background(accent, RoundedCornerShape(7.dp))
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("$value", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ButtonsCard(pressed: Set<String>) {
    val all = listOf(
        "A", "B", "X", "Y", "Up", "Down", "Left", "Right",
        "L", "R", "ZL", "ZR", "LS", "RS", "+", "-", "Home", "Camera", "Chat",
        "SL(L)", "SR(L)", "SL(R)", "SR(R)",
    )
    Column(
        Modifier.fillMaxWidth().background(cardBg, RoundedCornerShape(16.dp)).padding(14.dp)
    ) {
        Text("BUTTONS", color = textDim, fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(10.dp))
        // simple wrap layout
        FlowChips(all, pressed)
    }
}

@Composable
private fun FlowChips(all: List<String>, pressed: Set<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        all.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { name ->
                    val on = name in pressed
                    Box(
                        Modifier
                            .weight(1f)
                            .height(34.dp)
                            .background(
                                if (on) accent else Color(0xFF0E1116),
                                RoundedCornerShape(8.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            name,
                            color = if (on) Color(0xFF0E1116) else textDim,
                            fontSize = 11.sp,
                            fontWeight = if (on) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
                repeat(5 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
