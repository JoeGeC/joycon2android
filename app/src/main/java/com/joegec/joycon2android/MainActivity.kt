package com.joegec.joycon2android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import com.joegec.joycon2android.settings.presentation.SettingsPanel
import com.joegec.joycon2android.settings.presentation.SettingsPanelState
import com.joegec.joycon2android.settings.presentation.SettingsViewModel
import com.joegec.joycon2android.ui.components.EndDrawer
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.buttonmapping.presentation.ControllerMappingScreen
import com.joegec.joycon2android.buttonmapping.presentation.ControllerMappingViewModel
import com.joegec.joycon2android.buttonmapping.presentation.DeviceKeyWarnings
import com.joegec.joycon2android.buttonmapping.presentation.MappingActions
import com.joegec.joycon2android.dsu.presentation.DsuViewModel
import com.joegec.joycon2android.gamepad.presentation.GamepadViewModel
import com.joegec.joycon2android.ui.Joycon2ViewModel
import com.joegec.joycon2android.ui.JoyconScreen
import com.joegec.joycon2android.ui.pushTransition
import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.ui.components.CloseEmulatorDialog
import com.joegec.joycon2android.ui.components.EmulatorOption
import com.joegec.joycon2android.ui.components.StartEmulatorDialog
import com.joegec.joycon2android.ui.components.ProvideTips
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.dsu.presentation.DsuSlotLimitWarning
import com.joegec.joycon2android.dsu.presentation.dsuPortText
import com.joegec.joycon2android.output.OutputMode
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.presentation.OutputCard
import com.joegec.joycon2android.output.presentation.OutputCardActions
import com.joegec.joycon2android.output.presentation.OutputCardState
import com.joegec.joycon2android.output.presentation.OutputViewModel
import com.joegec.joycon2android.dsu.presentation.DsuSetupSheet
import com.joegec.joycon2android.update.presentation.UpdateDialog
import com.joegec.joycon2android.update.presentation.UpdateViewModel
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.Joycon2AndroidTheme

class MainActivity : ComponentActivity() {

    private val viewModel: Joycon2ViewModel by viewModels()
    private val dsuViewModel: DsuViewModel by viewModels {
        viewModelFactory {
            initializer {
                val c = (application as JoyconApplication).container
                DsuViewModel(c.observeDsuStatus)
            }
        }
    }
    private val gamepadViewModel: GamepadViewModel by viewModels {
        viewModelFactory {
            initializer {
                val c = (application as JoyconApplication).container
                GamepadViewModel(
                    c.observeGamepadStatus,
                    c.observeShizukuAvailability,
                    c.observeKeyboardEnabled,
                    c.observeMouseEnabled,
                )
            }
        }
    }
    private val outputViewModel: OutputViewModel by viewModels {
        viewModelFactory {
            initializer {
                val c = (application as JoyconApplication).container
                OutputViewModel(
                    c.observeOutputSelection,
                    c.observeOutputRunning,
                    c.setOutputRunning,
                    c.setOutputMode,
                    c.selectEmulator,
                    emulators = c.emulatorTargets,
                    configure = c::configureEmulator,
                )
            }
        }
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        viewModelFactory {
            initializer {
                val c = (application as JoyconApplication).container
                SettingsViewModel(
                    c.observeOutputSettings,
                    c.setFasterUpdates,
                    c.setBlockDeviceMotion,
                    c.observeShowTips,
                    c.setShowTips,
                    c.observeLaunchOnMainScreen,
                    c.setLaunchOnMainScreen,
                )
            }
        }
    }
    private val updateViewModel: UpdateViewModel by viewModels {
        viewModelFactory {
            initializer {
                val c = (application as JoyconApplication).container
                UpdateViewModel(c.checkForUpdate, c.skipUpdate, c.installUpdate)
            }
        }
    }
    private val controllerMappingViewModel: ControllerMappingViewModel by viewModels {
        viewModelFactory {
            initializer {
                val c = (application as JoyconApplication).container
                ControllerMappingViewModel(
                    c.observeGlobalMapping,
                    c.observeSavedLayouts,
                    c.applyMappingLayout,
                    c.applyGlobalLayout,
                    c.setControllerMapping,
                    c.setSidewaysRemote,
                    c.setThrust,
                    c.setSensitivity,
                    c.setProController,
                    c.saveCustomLayout,
                    c.saveGlobalLayout,
                    c.deleteCustomLayout,
                    c.deleteGlobalLayout,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.recheckPermissions()
        viewModel.recheckBatteryOptimization()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The UI is always dark; following a light-mode device would draw bar icons dark-on-dark.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)

        val permissionHandler = viewModel.permissionHandler

        val permLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { grants ->
            if (grants.values.all { it }) {
                viewModel.onPermissionsGranted()
                viewModel.startScan()
            } else {
                viewModel.onPermissionsDenied()
            }
        }

        setContent {
            val showTips by settingsViewModel.showTips.collectAsState()
            Joycon2AndroidTheme {
                ProvideTips(visible = showTips, onTurnOff = { settingsViewModel.toggleShowTips(false) }) {
                    Surface(Modifier.fillMaxSize(), color = Background) {
                        var mappingRoute by rememberSaveable { mutableStateOf<MappingRoute?>(null) }

                        UpdatePrompt()

                        AnimatedContent(
                            targetState = mappingRoute,
                            transitionSpec = { pushTransition(forward = targetState != null) },
                            label = "mappingScreen",
                        ) { route ->
                            Box(Modifier.fillMaxSize().background(Background)) {
                                if (route != null) {
                                    ControllerMappingRoute(route, onBack = { mappingRoute = null })
                                } else {
                                    MainRoute(
                                        onScan = { permLauncher.launch(permissionHandler.requiredPermissions) },
                                        onOpenMapping = { mappingRoute = it },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun UpdatePrompt() {
        val update by updateViewModel.availableUpdate.collectAsState()
        val progress by updateViewModel.installProgress.collectAsState()

        update?.let {
            UpdateDialog(
                update = it,
                progress = progress,
                onInstall = updateViewModel::install,
                onSkip = updateViewModel::skip,
                onDismiss = updateViewModel::dismiss,
            )
        }
    }

    @Composable
    private fun StartEmulatorPrompt(emulator: EmulatorOption?, onDismiss: () -> Unit) {
        if (emulator == null) return
        StartEmulatorDialog(
            emulatorName = emulator.label,
            onConfirm = {
                onDismiss()
                val container = (application as JoyconApplication).container
                val onMainScreen = settingsViewModel.launchOnMainScreen.value &&
                    container.deviceScreens.canLaunchOnMainScreen()
                container.emulatorLauncher.launch(emulator.id, onMainScreen)
            },
            onDismiss = onDismiss,
        )
    }

    @Composable
    private fun ControllerMappingRoute(route: MappingRoute, onBack: () -> Unit) {
        val console = route.console
        val players by viewModel.heldControllers.collectAsState()
        val bodies = players.mapNotNull { it.body() }
        val state by controllerMappingViewModel.uiState.collectAsState()
        val shizukuAvailable by gamepadViewModel.shizukuAvailable.collectAsState()
        var showDsuHelp by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(console, bodies) { controllerMappingViewModel.edit(console, bodies) }

        state?.let {
            ControllerMappingScreen(
                state = it,
                players = players,
                actions = mappingActions,
                onBack = onBack,
                onInfoClick = if (route.fromDsu) ({ showDsuHelp = true }) else null,
                viaDsu = route.fromDsu,
                deviceKeyWarnings = DeviceKeyWarnings(
                    androidNeedsShizuku = route.fromDsu && !shizukuAvailable,
                    keyboardAndMouseNeedShizuku = !shizukuAvailable,
                ),
            )
        }
        if (showDsuHelp) DsuSetupSheet(onDismiss = { showDsuHelp = false })
    }

    private val mappingActions = MappingActions(
        selectLayout = { body, layoutId -> controllerMappingViewModel.selectLayout(body, layoutId) },
        selectGlobalLayout = { controllerMappingViewModel.selectGlobalLayout(it) },
        saveLayout = { body, name -> controllerMappingViewModel.saveLayout(body, name) },
        deleteLayout = { layoutId, global -> controllerMappingViewModel.deleteLayout(layoutId, global) },
        setMapping = { body, targetKey, sourceId ->
            controllerMappingViewModel.setMapping(body, targetKey, sourceId)
        },
        setSidewaysRemote = { body, enabled ->
            controllerMappingViewModel.setSidewaysRemoteEnabled(body, enabled)
        },
        setThrust = { body, enabled -> controllerMappingViewModel.setThrustEnabled(body, enabled) },
        setSensitivity = { body, percent -> controllerMappingViewModel.setSensitivityPercent(body, percent) },
        setProController = { body, enabled -> controllerMappingViewModel.setProControllerEnabled(body, enabled) },
    )

    @Composable
    private fun MainRoute(onScan: () -> Unit, onOpenMapping: (MappingRoute) -> Unit) {
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        SettingsDrawer(drawerState) {
            MainScreen(onScan, onOpenMapping, onOpenSettings = { scope.launch { drawerState.open() } })
        }
    }

    @Composable
    private fun SettingsDrawer(drawerState: DrawerState, content: @Composable () -> Unit) {
        val outputSettings by settingsViewModel.outputSettings.collectAsState()
        val viewMode by viewModel.viewMode.collectAsState()
        val shizukuAvailable by gamepadViewModel.shizukuAvailable.collectAsState()
        val showTips by settingsViewModel.showTips.collectAsState()
        val launchOnMainScreen by settingsViewModel.launchOnMainScreen.collectAsState()
        val mainScreenLaunchAvailable = remember {
            (application as JoyconApplication).container.deviceScreens.canLaunchOnMainScreen()
        }
        EndDrawer(
            drawerState = drawerState,
            drawerContent = {
                SettingsPanel(
                    state = SettingsPanelState(
                        viewMode,
                        outputSettings,
                        shizukuAvailable = shizukuAvailable,
                        showTips = showTips,
                        mainScreenLaunchAvailable = mainScreenLaunchAvailable,
                        launchOnMainScreen = launchOnMainScreen,
                    ),
                    onViewModeChange = viewModel::setViewMode,
                    onFasterUpdatesToggle = settingsViewModel::toggleFasterUpdates,
                    onBlockDeviceMotionToggle = settingsViewModel::toggleBlockDeviceMotion,
                    onShowTipsToggle = settingsViewModel::toggleShowTips,
                    onLaunchOnMainScreenToggle = settingsViewModel::toggleLaunchOnMainScreen,
                )
            },
            containerColor = Background,
            content = content,
        )
    }

    @Composable
    private fun MainScreen(onScan: () -> Unit, onOpenMapping: (MappingRoute) -> Unit, onOpenSettings: () -> Unit) {
        val state by viewModel.session.collectAsState()
        val shizukuAvailable by gamepadViewModel.shizukuAvailable.collectAsState()
        val emulatorToClose by outputViewModel.emulatorToClose.collectAsState()
        val emulatorToStart by outputViewModel.emulatorToStart.collectAsState()
        val permissionDenied by viewModel.permissionDenied.collectAsState()
        val batteryRestricted by viewModel.batteryRestricted.collectAsState()
        val viewMode by viewModel.viewMode.collectAsState()

        // A written config matches one assignment, so Done/Failed goes stale when it changes.
        val assignmentKey = state.players.map {
            Triple(it.player.index, it.left?.address, it.right?.address)
        }
        LaunchedEffect(assignmentKey) { outputViewModel.resetSetupPhase() }

        emulatorToClose?.let { emulator ->
            CloseEmulatorDialog(
                emulatorName = emulator.label,
                onConfirm = { outputViewModel.closeEmulatorAndSetUp(state.activePlayers) },
                onDismiss = outputViewModel::cancelClose,
            )
        }
        StartEmulatorPrompt(emulatorToStart, outputViewModel::dismissStart)

        JoyconScreen(
            state = state,
            liveInputs = viewModel.liveInputs,
            outputCard = { modifier -> MainOutputCard(state.activePlayers, onOpenMapping, modifier) },
            permissionDenied = permissionDenied,
            batteryRestricted = batteryRestricted,
            onAllowBackgroundRunning = {
                startActivity(viewModel.batteryOptimizationExemption.buildRequestIntent())
            },
            onScan = onScan,
            onDisconnectAll = viewModel::disconnectAll,
            onAssign = viewModel::assignToPlayer,
            onUnassign = viewModel::unassign,
            onDisconnect = viewModel::disconnect,
            onSetControllerType = viewModel::setControllerType,
            onOpenSystemSettings = { startActivity(viewModel.permissionHandler.buildSettingsIntent()) },
            onOpenSettings = onOpenSettings,
            shizukuAvailable = shizukuAvailable,
            viewMode = viewMode,
        )
    }

    @Composable
    private fun MainOutputCard(
        players: List<PlayerState>,
        onOpenMapping: (MappingRoute) -> Unit,
        modifier: Modifier,
    ) {
        val container = (application as JoyconApplication).container
        val selection by outputViewModel.selection.collectAsState()
        val running by outputViewModel.running.collectAsState()
        val setupPhase by outputViewModel.setupPhase.collectAsState()
        val gamepadStatus by gamepadViewModel.status.collectAsState()
        val keyboardEnabled by gamepadViewModel.keyboardEnabled.collectAsState()
        val mouseEnabled by gamepadViewModel.mouseEnabled.collectAsState()
        val dsuStatus by dsuViewModel.status.collectAsState()
        val live = buildSet {
            if (gamepadStatus.enabled) add(OutputDevice.PAD)
            if (dsuStatus.enabled) add(OutputDevice.MOTION)
            if (keyboardEnabled) add(OutputDevice.KEYBOARD)
            if (mouseEnabled) add(OutputDevice.MOUSE)
        }

        OutputCard(
            state = OutputCardState(
                running = running,
                selection = selection,
                emulators = outputViewModel.emulators,
                setupPhase = setupPhase,
                liveDevices = live,
                error = gamepadStatus.error ?: dsuStatus.error,
                motionDetail = if (dsuStatus.enabled) dsuPortText(dsuStatus.clientCount) else null,
            ),
            actions = OutputCardActions(
                toggle = outputViewModel::toggle,
                setMode = outputViewModel::setMode,
                selectEmulator = outputViewModel::selectEmulator,
                setUp = { outputViewModel.setUp(players) },
                openMapping = {
                    // DSU inputs are picked from a list; detection never sees them.
                    val fromDsu = selection.mode == OutputMode.MOTION
                    onOpenMapping(MappingRoute(container.activeConsole.value, fromDsu))
                },
            ),
            modifier = modifier,
        ) {
            DsuSlotLimitWarning(dsuStatus, DsuSlots.coverage(players))
        }
    }
}
