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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.ui.components.AssignmentPanel
import com.joegec.joycon2android.ui.components.PlayerView
import com.joegec.joycon2android.ui.components.ScanningIndicator
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.ErrorBg
import com.joegec.joycon2android.ui.theme.ErrorText
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoyconScreen(
    state: AppUiState,
    onScan: () -> Unit,
    onDisconnectAll: () -> Unit,
    onAssign: (String, PlayerNumber) -> Unit,
    onUnassign: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { AppTitle(state) },
                actions = { ScanAction(state, onScan) },
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
            when {
                !state.anyConnected && !state.scanning -> IdleContent(state, onScan)
                state.anyConnected -> ConnectedContent(state, onScan, onDisconnectAll, onAssign, onUnassign)
                else -> ScanningContent(state)
            }
        }
    }
}

@Composable
private fun AppTitle(state: AppUiState) {
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
private fun ScanAction(state: AppUiState, onScan: () -> Unit) {
    if (!state.anyConnected) return

    TextButton(onClick = onScan, enabled = !state.scanning) {
        if (state.scanning) {
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
            color = if (state.scanning) TextDim else Accent,
            fontWeight = FontWeight.Bold,
            fontSize = Dimens.fontSizeBody,
        )
    }
}

@Composable
private fun statusText(state: AppUiState): String {
    val totalConnected = state.unassignedJoycons.size + state.activePlayers.sumOf {
        (if (it.left != null) 1 else 0) + (if (it.right != null) 1 else 0) as Int
    }
    return when {
        totalConnected > 0 -> stringResource(R.string.status_connected_count, totalConnected)
        state.scanning -> stringResource(R.string.status_scanning)
        else -> stringResource(R.string.status_disconnected)
    }
}

@Composable
private fun IdleContent(state: AppUiState, onScan: () -> Unit) {
    Button(
        onClick = onScan,
        modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeightLarge),
        shape = RoundedCornerShape(Dimens.buttonCorner),
        colors = ButtonDefaults.buttonColors(containerColor = Accent),
    ) {
        Text(
            stringResource(R.string.button_scan_connect),
            color = TextOnAccent,
            fontWeight = FontWeight.Bold,
            fontSize = Dimens.fontSizeButtonLarge,
        )
    }

    ErrorMessage(state.error)

    Text(
        stringResource(R.string.scan_idle_hint),
        color = TextDim,
        fontSize = Dimens.fontSizeMedium,
    )
}

@Composable
private fun ScanningContent(state: AppUiState) {
    ScanningIndicator()
    ErrorMessage(state.error)
}

@Composable
private fun ConnectedContent(
    state: AppUiState,
    onScan: () -> Unit,
    onDisconnectAll: () -> Unit,
    onAssign: (String, PlayerNumber) -> Unit,
    onUnassign: (String) -> Unit,
) {
    if (state.unassignedJoycons.isNotEmpty()) {
        AssignmentPanel(
            unassigned = state.unassignedJoycons,
            onAssign = onAssign,
        )
    }

    state.activePlayers.forEach { playerState ->
        PlayerView(
            playerState = playerState,
            onUnassign = onUnassign,
        )
    }

    if (state.scanning) {
        ScanningIndicator()
    }

    ErrorMessage(state.error)

    OutlinedButton(
        onClick = onDisconnectAll,
        modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.buttonCorner),
    ) {
        Text(stringResource(R.string.button_disconnect_all), color = TextDim)
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
