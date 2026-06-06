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
import com.joegec.joycon2android.ui.theme.TextOnAccent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.ui.components.ControllerLayout
import com.joegec.joycon2android.ui.components.ScanningIndicator
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.ErrorBg
import com.joegec.joycon2android.ui.theme.ErrorText
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
                title = { AppTitle(state) },
                actions = { ScanAction(state, onConnect) },
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
                .padding(horizontal = Dimens.screenPaddingHorizontal)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
        ) {
            if (state.anyConnected) {
                ConnectedContent(state, onStop)
            } else {
                DisconnectedContent(state, onConnect)
            }
        }
    }
}

@Composable
private fun AppTitle(state: ControllerState) {
    Column {
        Text(
            stringResource(R.string.app_title),
            color = Color.White,
            fontSize = Dimens.fontSizeTitle,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp,
        )
        Text(
            statusText(state),
            color = if (state.anyConnected) Accent else TextDim,
            fontSize = Dimens.fontSizeStatus,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ScanAction(state: ControllerState, onConnect: () -> Unit) {
    if (!state.anyConnected || state.bothConnected) return

    val isBusy = state.scanning || state.anyConnecting
    TextButton(onClick = onConnect, enabled = !isBusy) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = Accent,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.size(6.dp))
        }
        Text(
            if (state.scanning) stringResource(R.string.status_scanning)
            else stringResource(R.string.button_scan),
            color = if (isBusy) TextDim else Accent,
            fontWeight = FontWeight.Bold,
            fontSize = Dimens.fontSizeBody,
        )
    }
}

@Composable
private fun statusText(state: ControllerState): String = when {
    state.bothConnected -> stringResource(R.string.status_both_connected)
    state.left.connected -> stringResource(R.string.status_left_connected)
    state.right.connected -> stringResource(R.string.status_right_connected)
    state.anyConnecting -> stringResource(R.string.status_connecting)
    state.scanning -> stringResource(R.string.status_scanning)
    else -> stringResource(R.string.status_disconnected)
}

@Composable
private fun DisconnectedContent(state: ControllerState, onConnect: () -> Unit) {
    val isBusy = state.scanning || state.anyConnecting

    ScanConnectButton(state, isBusy, onConnect)

    if (state.scanning) {
        ScanningIndicator()
    }

    FoundDevices(state)
    ErrorMessage(state.error)

    if (!isBusy && state.error == null) {
        Text(
            stringResource(R.string.scan_idle_hint),
            color = TextDim,
            fontSize = Dimens.fontSizeMedium,
        )
    }
}

@Composable
private fun ScanConnectButton(state: ControllerState, isBusy: Boolean, onConnect: () -> Unit) {
    Button(
        onClick = onConnect,
        modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightLarge),
        shape = RoundedCornerShape(Dimens.buttonCorner),
        colors = ButtonDefaults.buttonColors(containerColor = Accent),
        enabled = !isBusy,
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = TextOnAccent,
                strokeWidth = 2.dp,
            )
            Spacer(Modifier.size(10.dp))
        }
        Text(
            when {
                state.anyConnecting -> stringResource(R.string.status_connecting)
                state.scanning -> stringResource(R.string.status_scanning)
                else -> stringResource(R.string.button_scan_connect)
            },
            color = TextOnAccent,
            fontWeight = FontWeight.Bold,
            fontSize = Dimens.fontSizeButtonLarge,
        )
    }
}

@Composable
private fun FoundDevices(state: ControllerState) {
    if (state.left.connecting && state.left.deviceName != null) {
        Text(
            stringResource(R.string.found_device, state.left.deviceName),
            color = Accent,
            fontSize = Dimens.fontSizeBody,
            fontWeight = FontWeight.Medium,
        )
    }
    if (state.right.connecting && state.right.deviceName != null) {
        Text(
            stringResource(R.string.found_device, state.right.deviceName),
            color = Accent,
            fontSize = Dimens.fontSizeBody,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ErrorMessage(error: String?) {
    if (error == null) return
    Box(
        Modifier
            .fillMaxWidth()
            .background(ErrorBg, RoundedCornerShape(Dimens.buttonCorner))
            .padding(Dimens.cardPadding)
    ) {
        Text(error, color = ErrorText, fontSize = Dimens.fontSizeBody)
    }
}

@Composable
private fun ConnectedContent(state: ControllerState, onStop: () -> Unit) {
    ControllerLayout(state)

    OutlinedButton(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.buttonCorner),
    ) {
        Text(stringResource(R.string.button_disconnect), color = TextDim)
    }
}
