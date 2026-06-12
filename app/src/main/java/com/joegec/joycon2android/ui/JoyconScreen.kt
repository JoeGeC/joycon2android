package com.joegec.joycon2android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.ui.components.AdbSetupCard
import com.joegec.joycon2android.ui.components.AdbSetupState
import com.joegec.joycon2android.ui.components.AssignmentPanel
import com.joegec.joycon2android.ui.components.DsuCard
import com.joegec.joycon2android.ui.components.DsuCardState
import com.joegec.joycon2android.ui.components.ErrorBox
import com.joegec.joycon2android.ui.components.FeatureToggleCard
import com.joegec.joycon2android.ui.components.PlayerView
import com.joegec.joycon2android.ui.components.ScanningIndicator
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.JoyconBlue
import com.joegec.joycon2android.ui.theme.JoyconRed
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoyconScreen(
    state: AppUiState,
    gamepadEnabled: Boolean,
    gamepadError: String?,
    dsuState: DsuCardState,
    adbSetup: AdbSetupState,
    permissionDenied: Boolean,
    onScan: () -> Unit,
    onDisconnectAll: () -> Unit,
    onAssign: (String, PlayerNumber) -> Unit,
    onUnassign: (String) -> Unit,
    onDisconnect: (String) -> Unit,
    onGamepadToggle: (Boolean) -> Unit,
    onDsuToggle: (Boolean) -> Unit,
    onDsuLanToggle: (Boolean) -> Unit,
    onEnableNotifications: () -> Unit,
    onStartAdbPairing: () -> Unit,
    onOpenSettings: () -> Unit,
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
        val screenState = when {
            !state.anyConnected && !state.scanning -> ScreenState.IDLE
            !state.anyConnected -> ScreenState.SCANNING
            else -> ScreenState.CONNECTED
        }

        LaunchedEffect(screenState) {
            if (screenState == ScreenState.IDLE) {
                scrollBehavior.state.heightOffset = 0f
            }
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            KofiBanner()

            Crossfade(targetState = screenState, modifier = Modifier.weight(1f), label = "screen") { target ->
                when (target) {
                    ScreenState.IDLE -> IdleContent(
                        state, permissionDenied, onScan, onOpenSettings,
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.screenPaddingHorizontal),
                    )
                    ScreenState.SCANNING, ScreenState.CONNECTED -> Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.screenPaddingHorizontal)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
                    ) {
                        when (target) {
                            ScreenState.CONNECTED -> ConnectedContent(
                                state, gamepadEnabled, gamepadError, dsuState, adbSetup,
                                onDisconnectAll, onAssign, onUnassign, onDisconnect,
                                onGamepadToggle, onDsuToggle, onDsuLanToggle,
                                onEnableNotifications, onStartAdbPairing,
                            )
                            else -> ScanningContent(state)
                        }
                    }
                }
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
        AnimatedVisibility(
            visible = state.scanning,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Row {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = Accent,
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.size(6.dp))
            }
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
private fun IdleContent(
    state: AppUiState,
    permissionDenied: Boolean,
    onScan: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(bottom = Dimens.screenPaddingVertical),
        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
    ) {
        ErrorBox(text = state.error)
        ErrorBox(
            text = if (permissionDenied) stringResource(R.string.error_permissions_denied) else null,
            onClick = onOpenSettings,
        )

        Spacer(Modifier.weight(1f))

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
    }
}

@Composable
private fun ScanningContent(state: AppUiState) {
    ScanningIndicator()
    SyncButtonGraphic()
    ErrorBox(text = state.error)
}

@Composable
private fun SyncButtonGraphic(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.sync_button_graphic),
        contentDescription = stringResource(R.string.scanning_hint),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardCorner)),
    )
}


@Composable
private fun KofiBanner(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val shape = RoundedCornerShape(Dimens.cardCorner)
    val gradientBorder = Brush.linearGradient(listOf(JoyconBlue, JoyconRed))

    Row(
        modifier
            .padding(horizontal = Dimens.screenPaddingHorizontal)
            .padding(bottom = Dimens.sectionSpacing)
            .fillMaxWidth()
            .clip(shape)
            .border(Dimens.cardBorderWidth, gradientBorder, shape)
            .background(CardBg)
            .clickable { uriHandler.openUri("https://ko-fi.com/joycon2android") }
            .padding(Dimens.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.kofi),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Text(
            stringResource(R.string.kofi_banner),
            color = Color.White,
            fontSize = Dimens.fontSizeBody,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = TextDim,
        )
    }
}

@Composable
private fun ConnectedContent(
    state: AppUiState,
    gamepadEnabled: Boolean,
    gamepadError: String?,
    dsuState: DsuCardState,
    adbSetup: AdbSetupState,
    onDisconnectAll: () -> Unit,
    onAssign: (String, PlayerNumber) -> Unit,
    onUnassign: (String) -> Unit,
    onDisconnect: (String) -> Unit,
    onGamepadToggle: (Boolean) -> Unit,
    onDsuToggle: (Boolean) -> Unit,
    onDsuLanToggle: (Boolean) -> Unit,
    onEnableNotifications: () -> Unit,
    onStartAdbPairing: () -> Unit,
) {
    AnimatedVisibility(
        visible = state.unassignedJoycons.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        AssignmentPanel(
            unassigned = state.unassignedJoycons,
            players = state.players,
            onAssign = onAssign,
            onDisconnect = onDisconnect,
        )
    }

    state.activePlayers.forEach { playerState ->
        PlayerView(
            playerState = playerState,
            onUnassign = onUnassign,
        )
    }

    AnimatedVisibility(
        visible = state.activePlayers.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing)) {
            FeatureToggleCard(
                title = stringResource(R.string.gamepad_title),
                subtitle = stringResource(
                    if (gamepadEnabled) R.string.gamepad_subtitle_on
                    else R.string.gamepad_subtitle_off
                ),
                checked = gamepadEnabled,
                error = gamepadError,
                onToggle = onGamepadToggle,
            )
            if (adbSetup.needed) {
                AdbSetupCard(
                    state = adbSetup,
                    onEnableNotifications = onEnableNotifications,
                    onStartPairing = onStartAdbPairing,
                )
            }
            DsuCard(
                state = dsuState,
                onToggle = onDsuToggle,
                onLanToggle = onDsuLanToggle,
            )
        }
    }

    AnimatedVisibility(
        visible = state.scanning,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing)) {
            ScanningIndicator()
            SyncButtonGraphic()
        }
    }

    ErrorBox(text = state.error)

    OutlinedButton(
        onClick = onDisconnectAll,
        modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.buttonCorner),
    ) {
        Text(stringResource(R.string.button_disconnect_all), color = TextDim)
    }
}

private enum class ScreenState { IDLE, SCANNING, CONNECTED }
