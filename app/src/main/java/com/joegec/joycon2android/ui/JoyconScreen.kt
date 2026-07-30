package com.joegec.joycon2android.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.joegec.joycon2android.gamepad.presentation.ShizukuSetupCard
import com.joegec.joycon2android.assignment.presentation.AssignmentPanel
import com.joegec.joycon2android.connection.presentation.CompactPlayerRow
import com.joegec.joycon2android.model.ConnectionViewMode
import com.joegec.joycon2android.ui.components.DolphinSetupPhase
import com.joegec.joycon2android.ui.components.EmulatorAutoSetup
import com.joegec.joycon2android.ui.components.EmulatorOption
import com.joegec.joycon2android.dsu.presentation.DsuCard
import com.joegec.joycon2android.dsu.presentation.DsuCardState
import com.joegec.joycon2android.ui.components.ErrorBox
import com.joegec.joycon2android.ui.components.FeatureToggleCard
import com.joegec.joycon2android.connection.presentation.PlayerView
import com.joegec.joycon2android.connection.presentation.ViewModeToggle
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
    shizukuAvailable: Boolean,
    permissionDenied: Boolean,
    onScan: () -> Unit,
    onDisconnectAll: () -> Unit,
    onAssign: (String, PlayerNumber) -> Unit,
    onUnassign: (String) -> Unit,
    onDisconnect: (String) -> Unit,
    onGamepadToggle: (Boolean) -> Unit,
    gamepadEmulators: List<EmulatorOption>,
    selectedGamepadEmulator: String,
    onSelectGamepadEmulator: (String) -> Unit,
    gamepadSetupAvailable: Boolean,
    gamepadSetupPhase: DolphinSetupPhase,
    onConfigureGamepad: () -> Unit,
    onDsuToggle: (Boolean) -> Unit,
    onConfigureDolphin: () -> Unit,
    onOpenSettings: () -> Unit,
    viewMode: ConnectionViewMode,
    onViewModeChange: (ConnectionViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Background,
        // Keep the bottom inset out of the content padding so scrollable content can pass under
        // the nav bar; each screen re-applies it where its own content must stay clear of it.
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        topBar = {
            TopAppBar(
                title = { AppTitle(state, shizukuAvailable) },
                actions = {
                    if (state.activePlayers.isNotEmpty()) {
                        ViewModeToggle(
                            mode = viewMode,
                            onModeChange = onViewModeChange,
                            modifier = Modifier.padding(end = Dimens.elementSpacing),
                        )
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
                            .padding(horizontal = Dimens.screenPaddingHorizontal)
                            .windowInsetsPadding(WindowInsets.navigationBars),
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
                                state, viewMode, gamepadEnabled, gamepadError, dsuState, shizukuAvailable,
                                gamepadEmulators, selectedGamepadEmulator, onSelectGamepadEmulator,
                                gamepadSetupAvailable, gamepadSetupPhase, onConfigureGamepad,
                                onScan, onDisconnectAll, onAssign, onUnassign, onDisconnect,
                                onGamepadToggle, onDsuToggle, onConfigureDolphin,
                            )
                            else -> ScanningContent(state)
                        }
                        // Lets the last item scroll clear of the nav bar it now passes under
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }
        }
    }
}

@Composable
private fun AppTitle(state: AppUiState, shizukuAvailable: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo),
            contentDescription = stringResource(R.string.app_title),
            modifier = Modifier.size(Dimens.headerLogoSize),
        )
        Column {
            Text(
                statusText(state),
                color = if (state.anyConnected) Accent else TextDim,
                fontSize = Dimens.fontSizeStatus,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
            )
            PrivilegedAccessStatus(shizukuAvailable)
        }
    }
}

// Shizuku is the privileged backend for the /dev/uhid access the gamepad needs.
@Composable
private fun PrivilegedAccessStatus(shizukuAvailable: Boolean) {
    val color = if (shizukuAvailable) Accent else TextDim
    Row(
        horizontalArrangement = Arrangement.spacedBy(Dimens.statusDotGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(Dimens.statusDotSize)
                .background(color, CircleShape)
        )
        Text(
            stringResource(R.string.status_shizuku),
            color = color,
            fontSize = Dimens.fontSizeStatus,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Medium,
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
private fun ScanButton(onScan: () -> Unit) {
    Button(
        onClick = onScan,
        modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
        shape = RoundedCornerShape(Dimens.buttonCorner),
        colors = ButtonDefaults.buttonColors(containerColor = Accent),
    ) {
        Text(
            stringResource(R.string.button_scan),
            color = TextOnAccent,
            fontWeight = FontWeight.Bold,
            fontSize = Dimens.fontSizeButton,
        )
    }
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
    viewMode: ConnectionViewMode,
    gamepadEnabled: Boolean,
    gamepadError: String?,
    dsuState: DsuCardState,
    shizukuAvailable: Boolean,
    gamepadEmulators: List<EmulatorOption>,
    selectedGamepadEmulator: String,
    onSelectGamepadEmulator: (String) -> Unit,
    gamepadSetupAvailable: Boolean,
    gamepadSetupPhase: DolphinSetupPhase,
    onConfigureGamepad: () -> Unit,
    onScan: () -> Unit,
    onDisconnectAll: () -> Unit,
    onAssign: (String, PlayerNumber) -> Unit,
    onUnassign: (String) -> Unit,
    onDisconnect: (String) -> Unit,
    onGamepadToggle: (Boolean) -> Unit,
    onDsuToggle: (Boolean) -> Unit,
    onConfigureDolphin: () -> Unit,
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
        AnimatedContent(
            targetState = viewMode,
            transitionSpec = {
                (fadeIn(tween(220)) togetherWith fadeOut(tween(220)))
                    .using(SizeTransform(clip = false))
            },
            label = "viewMode",
        ) { mode ->
            when (mode) {
                ConnectionViewMode.DETAILED -> PlayerView(
                    playerState = playerState,
                    onUnassign = onUnassign,
                )
                ConnectionViewMode.COMPACT -> CompactPlayerRow(
                    playerState = playerState,
                    onUnassign = onUnassign,
                )
            }
        }
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
            ) {
                if (gamepadEnabled && gamepadEmulators.isNotEmpty() && gamepadSetupAvailable) {
                    Spacer(Modifier.height(Dimens.elementSpacing))
                    EmulatorAutoSetup(
                        emulators = gamepadEmulators,
                        selectedEmulator = selectedGamepadEmulator,
                        onSelectEmulator = onSelectGamepadEmulator,
                        phase = gamepadSetupPhase,
                        setupLabel = stringResource(R.string.gamepad_emulator_setup),
                        onSetUp = onConfigureGamepad,
                    )
                }
            }
            if (!shizukuAvailable) {
                ShizukuSetupCard()
            }
            DsuCard(
                state = dsuState,
                onToggle = onDsuToggle,
                onConfigureDolphin = onConfigureDolphin,
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

    AnimatedVisibility(
        visible = !state.scanning,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        ScanButton(onScan)
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
