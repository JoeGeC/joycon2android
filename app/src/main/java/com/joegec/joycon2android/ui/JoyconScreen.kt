package com.joegec.joycon2android.ui

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
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
import com.joegec.joycon2android.ui.theme.AppType
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.JoyconBlue
import com.joegec.joycon2android.ui.theme.JoyconRed
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextOnAccent
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Material 3 small top-app-bar container height; the app bar overlays the content, so screens add
// this (plus the status-bar inset) as top clearance rather than the Scaffold reserving it.
private val AppBarHeight = 64.dp

// Landscape packs two players per row, so each detailed controller is shrunk to help a full player
// fit the short landscape height.
private const val LandscapePlayerScale = 0.7f

/**
 * Lays the content out as if it had 1/[scale] the space, then draws it scaled down and reports the
 * smaller size — shrinking the whole controller (buttons, labels, spacing) uniformly while still
 * reflowing siblings, unlike a plain graphicsLayer scale which leaves the original bounds behind.
 */
private fun Modifier.scaleLayout(scale: Float): Modifier = layout { measurable, constraints ->
    fun up(value: Int) = (value / scale).roundToInt()
    val placeable = measurable.measure(
        constraints.copy(
            minWidth = up(constraints.minWidth),
            maxWidth = if (constraints.hasBoundedWidth) up(constraints.maxWidth) else constraints.maxWidth,
            minHeight = up(constraints.minHeight),
            maxHeight = if (constraints.hasBoundedHeight) up(constraints.maxHeight) else constraints.maxHeight,
        )
    )
    layout((placeable.width * scale).roundToInt(), (placeable.height * scale).roundToInt()) {
        placeable.placeWithLayer(0, 0) {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(0f, 0f)
        }
    }
}

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
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Unassigning is reachable by tapping the live display, so every removal is offered back as an
    // undo (re-assigning the same controllers to the same player) rather than being silent.
    fun offerUndo(message: String, restore: List<Pair<String, PlayerNumber>>) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = context.getString(R.string.action_undo),
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                restore.forEach { (address, player) -> onAssign(address, player) }
            }
        }
    }

    val unassignController: (String) -> Unit = { address ->
        val player = state.players.firstOrNull {
            it.left?.address == address || it.right?.address == address
        }?.player
        onUnassign(address)
        if (player != null) {
            offerUndo(context.getString(R.string.snackbar_controller_removed), listOf(address to player))
        }
    }

    val removePlayer: (PlayerState) -> Unit = { playerState ->
        val restore = listOfNotNull(playerState.left, playerState.right).map { it.address to playerState.player }
        restore.forEach { (address, _) -> onUnassign(address) }
        offerUndo(context.getString(R.string.snackbar_player_removed, playerState.player.index), restore)
    }

    Scaffold(
        modifier = modifier,
        containerColor = Background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                val dismissState = rememberSwipeToDismissBoxState()
                LaunchedEffect(dismissState.currentValue) {
                    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                        data.dismiss()
                    }
                }
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {},
                ) {
                    Snackbar(data)
                }
            }
        },
        // Only reserve the horizontal insets: content passes under both the status bar (as the app
        // bar collapses on scroll) and the nav bar. Each screen re-applies those where its own
        // content must stay clear of the system bars.
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        val screenState = when {
            !state.anyConnected && !state.scanning -> ScreenState.IDLE
            !state.anyConnected -> ScreenState.SCANNING
            else -> ScreenState.CONNECTED
        }

        LaunchedEffect(screenState) {
            if (screenState == ScreenState.IDLE) {
                scrollState.scrollTo(0)
            }
        }

        // The app bar overlays the content instead of reserving space, so the scroll passes behind
        // the transparent status bar; each screen adds the bar's height back as top clearance, and
        // the bar itself is translated up in lockstep with the scroll so it slides away without a gap.
        val appBarSpace = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + AppBarHeight
        val appBarSpacePx = with(LocalDensity.current) { appBarSpace.toPx() }

        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = screenState, modifier = Modifier.fillMaxSize(), label = "screen") { target ->
                when (target) {
                    ScreenState.IDLE -> IdleContent(
                        state, permissionDenied, onScan, onOpenSettings,
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.screenPaddingHorizontal)
                            .padding(top = appBarSpace)
                            .windowInsetsPadding(WindowInsets.navigationBars),
                    )
                    ScreenState.SCANNING, ScreenState.CONNECTED -> Column(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = Dimens.screenPaddingHorizontal)
                            .verticalScroll(scrollState)
                            .padding(top = appBarSpace),
                        verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
                    ) {
                        KofiBanner()
                        when (target) {
                            ScreenState.CONNECTED -> ConnectedContent(
                                state, viewMode, gamepadEnabled, gamepadError, dsuState, shizukuAvailable,
                                gamepadEmulators, selectedGamepadEmulator, onSelectGamepadEmulator,
                                gamepadSetupAvailable, gamepadSetupPhase, onConfigureGamepad,
                                onScan, onDisconnectAll, onAssign, unassignController, removePlayer, onDisconnect,
                                onGamepadToggle, onDsuToggle, onConfigureDolphin,
                            )
                            else -> ScanningContent(state)
                        }
                        // Lets the last item scroll clear of the nav bar it now passes under
                        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
                }
            }

            // Overlaid so content scrolls behind it and the transparent status bar; collapses on scroll.
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
                modifier = Modifier.graphicsLayer {
                    translationY = if (screenState == ScreenState.IDLE) 0f
                    else -scrollState.value.toFloat().coerceIn(0f, appBarSpacePx)
                },
            )
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
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing)
        ) {
            Text(
                statusText(state),
                color = if (state.anyConnected) Accent else TextDim,
                style = AppType.statusOverline,
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
            style = AppType.statusOverline,
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
        KofiBanner()
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
                style = MaterialTheme.typography.titleMedium,
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
            style = MaterialTheme.typography.labelLarge,
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
            style = MaterialTheme.typography.bodyMedium,
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
    onRemovePlayer: (PlayerState) -> Unit,
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

    val landscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (landscape && viewMode == ConnectionViewMode.DETAILED) {
        // Two players per row, each shrunk so a full controller is more likely to fit the height.
        state.activePlayers.chunked(2).forEach { rowPlayers ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing)) {
                rowPlayers.forEach { playerState ->
                    PlayerView(
                        playerState = playerState,
                        onUnassign = onUnassign,
                        onRemovePlayer = { onRemovePlayer(playerState) },
                        modifier = Modifier
                            .weight(1f)
                            .scaleLayout(LandscapePlayerScale),
                    )
                }
                if (rowPlayers.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    } else {
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
                        onRemovePlayer = { onRemovePlayer(playerState) },
                    )
                    ConnectionViewMode.COMPACT -> CompactPlayerRow(
                        playerState = playerState,
                        onUnassign = onUnassign,
                        onRemovePlayer = { onRemovePlayer(playerState) },
                    )
                }
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
