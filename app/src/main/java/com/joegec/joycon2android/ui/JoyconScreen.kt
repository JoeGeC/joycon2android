package com.joegec.joycon2android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.ui.components.ControllerLayout
import com.joegec.joycon2android.ui.components.ScanningIndicator
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.TextDim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoyconScreen(
    state: ControllerState,
    onConnect: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "JOY-CON 2",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp,
                        )
                        Text(
                            statusText(state),
                            color = if (state.anyConnected) Accent else TextDim,
                            fontSize = 11.sp,
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                },
                actions = {
                    if (state.anyConnected && !state.bothConnected) {
                        val isBusy = state.scanning || state.anyConnecting
                        TextButton(
                            onClick = onConnect,
                            enabled = !isBusy,
                        ) {
                            if (isBusy) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = Accent,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.size(6.dp))
                            }
                            Text(
                                if (state.scanning) "Scanning…" else "Scan",
                                color = if (isBusy) TextDim else Accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    scrolledContainerColor = Background,
                ),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.anyConnected) {
                ConnectedContent(state, onStop)
            } else {
                DisconnectedContent(state, onConnect)
            }
        }
    }
}

private fun statusText(state: ControllerState): String = when {
    state.bothConnected -> "BOTH CONNECTED"
    state.left.connected -> "LEFT CONNECTED"
    state.right.connected -> "RIGHT CONNECTED"
    state.anyConnecting -> "CONNECTING…"
    state.scanning -> "SCANNING…"
    else -> "DISCONNECTED"
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

    OutlinedButton(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth().height(44.dp),
        shape = RoundedCornerShape(12.dp),
    ) { Text("Disconnect", color = TextDim) }
}
