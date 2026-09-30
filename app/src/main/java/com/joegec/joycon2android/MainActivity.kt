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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.buttonmapping.presentation.ControllerMappingScreen
import com.joegec.joycon2android.buttonmapping.presentation.ControllerMappingViewModel
import com.joegec.joycon2android.buttonmapping.presentation.MappingActions
import com.joegec.joycon2android.dsu.presentation.DsuViewModel
import com.joegec.joycon2android.gamepad.presentation.GamepadViewModel
import com.joegec.joycon2android.ui.Joycon2ViewModel
import com.joegec.joycon2android.ui.JoyconScreen
import com.joegec.joycon2android.ui.pushTransition
import com.joegec.joycon2android.dsu.DsuSlots
import com.joegec.joycon2android.emulatorconfig.EdenPaths
import com.joegec.joycon2android.ui.components.CloseEmulatorDialog
import com.joegec.joycon2android.ui.components.EmulatorOption
import com.joegec.joycon2android.ui.components.StartEmulatorDialog
import com.joegec.joycon2android.dsu.presentation.DsuCardState
import com.joegec.joycon2android.dsu.presentation.DsuMappingHelpSheet
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
                DsuViewModel(
                    c.observeDsuStatus,
                    c.enableDsu,
                    c.disableDsu,
                    dsuEmulators = c.emulatorSetup.dsuEmulators(),
                    configureDsu = c.emulatorSetup::configureDsu,
                )
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
                    c.enableGamepad,
                    c.disableGamepad,
                    gamepadEmulators = c.emulatorSetup.gamepadEmulators(),
                    configureGamepad = c.emulatorSetup::configureGamepad,
                )
            }
        }
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        viewModelFactory {
            initializer {
                val c = (application as JoyconApplication).container
                SettingsViewModel(c.observeOutputSettings, c.setFasterUpdates, c.setBlockDeviceMotion)
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
            Joycon2AndroidTheme {
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
                (application as JoyconApplication).container.emulatorLauncher.launch(emulator.id)
            },
            onDismiss = onDismiss,
        )
    }

    @Composable
    private fun ControllerMappingRoute(route: MappingRoute, onBack: () -> Unit) {
        val console = route.console
        val session by viewModel.uiState.collectAsState()
        val players = session.activePlayers
        val bodies = players.mapNotNull { it.body() }
        val state by controllerMappingViewModel.uiState.collectAsState()
        var showDsuHelp by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(console, bodies) { controllerMappingViewModel.edit(console, bodies) }

        state?.let {
            ControllerMappingScreen(
                state = it,
                players = players,
                actions = mappingActions,
                onBack = onBack,
                onInfoClick = if (route.fromDsu) ({ showDsuHelp = true }) else null,
            )
        }
        if (showDsuHelp) {
            val dsuStatus by dsuViewModel.status.collectAsState()
            DsuMappingHelpSheet(address = dsuStatus.address, onDismiss = { showDsuHelp = false })
        }
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
        EndDrawer(
            drawerState = drawerState,
            drawerContent = {
                SettingsPanel(
                    state = SettingsPanelState(viewMode, outputSettings, deviceMotionBlockAvailable = shizukuAvailable),
                    onViewModeChange = viewModel::setViewMode,
                    onFasterUpdatesToggle = settingsViewModel::toggleFasterUpdates,
                    onBlockDeviceMotionToggle = settingsViewModel::toggleBlockDeviceMotion,
                )
            },
            containerColor = Background,
            content = content,
        )
    }

    @Composable
    private fun MainScreen(onScan: () -> Unit, onOpenMapping: (MappingRoute) -> Unit, onOpenSettings: () -> Unit) {
        val state by viewModel.uiState.collectAsState()
        val gamepadStatus by gamepadViewModel.status.collectAsState()
        val shizukuAvailable by gamepadViewModel.shizukuAvailable.collectAsState()
        val dsuStatus by dsuViewModel.status.collectAsState()
        val dsuSetupPhase by dsuViewModel.setupPhase.collectAsState()
        val selectedDsuEmulator by dsuViewModel.selectedEmulator.collectAsState()
        val dsuEmulatorToClose by dsuViewModel.emulatorToClose.collectAsState()
        val gamepadEmulatorToClose by gamepadViewModel.emulatorToClose.collectAsState()
        val dsuEmulatorToStart by dsuViewModel.emulatorToStart.collectAsState()
        val gamepadEmulatorToStart by gamepadViewModel.emulatorToStart.collectAsState()
        val gamepadSetupPhase by gamepadViewModel.setupPhase.collectAsState()
        val selectedEmulator by gamepadViewModel.selectedEmulator.collectAsState()
        val permissionDenied by viewModel.permissionDenied.collectAsState()
        val viewMode by viewModel.viewMode.collectAsState()

        // A written config matches one assignment, so Done/Failed goes stale when it changes.
        val assignmentKey = state.players.map {
            Triple(it.player.index, it.left?.address, it.right?.address)
        }
        LaunchedEffect(assignmentKey) {
            dsuViewModel.resetSetupPhase()
            gamepadViewModel.resetSetupPhase()
        }

        dsuEmulatorToClose?.let { emulator ->
            CloseEmulatorDialog(
                emulatorName = emulator.label,
                onConfirm = { dsuViewModel.closeEmulatorAndConfigure(state.activePlayers) },
                onDismiss = dsuViewModel::cancelClose,
            )
        }
        gamepadEmulatorToClose?.let { emulator ->
            CloseEmulatorDialog(
                emulatorName = emulator.label,
                onConfirm = { gamepadViewModel.closeEmulatorAndConfigure(state.activePlayers) },
                onDismiss = gamepadViewModel::cancelClose,
            )
        }
        StartEmulatorPrompt(dsuEmulatorToStart, dsuViewModel::dismissStart)
        StartEmulatorPrompt(gamepadEmulatorToStart, gamepadViewModel::dismissStart)

        JoyconScreen(
            state = state,
            gamepadEnabled = gamepadStatus.enabled,
            gamepadError = gamepadStatus.error,
            dsuState = DsuCardState(
                enabled = dsuStatus.enabled,
                error = dsuStatus.error,
                clientCount = dsuStatus.clientCount,
                coverage = DsuSlots.coverage(state.activePlayers),
                emulators = dsuViewModel.dsuEmulators,
                selectedEmulator = selectedDsuEmulator,
                setupPhase = dsuSetupPhase,
            ),
            permissionDenied = permissionDenied,
            onScan = onScan,
            onDisconnectAll = viewModel::disconnectAll,
            onAssign = viewModel::assignToPlayer,
            onUnassign = viewModel::unassign,
            onDisconnect = viewModel::disconnect,
            onGamepadToggle = { enabled ->
                gamepadViewModel.toggle(enabled, state.activePlayers)
            },
            gamepadEmulators = gamepadViewModel.gamepadEmulators,
            selectedGamepadEmulator = selectedEmulator,
            onSelectGamepadEmulator = gamepadViewModel::selectEmulator,
            gamepadSetupPhase = gamepadSetupPhase,
            onConfigureGamepad = { gamepadViewModel.configureGamepad(state.activePlayers) },
            onOpenGamepadMapping = {
                val console = if (selectedEmulator in EdenPaths.PACKAGES) Console.SWITCH_PRO else Console.GAMECUBE
                onOpenMapping(MappingRoute(console, fromDsu = false))
            },
            onDsuToggle = dsuViewModel::toggle,
            onSelectDsuEmulator = dsuViewModel::selectEmulator,
            onConfigureDsu = { dsuViewModel.configureDsu(state.activePlayers) },
            onOpenDsuMapping = {
                val console = if (selectedDsuEmulator in EdenPaths.PACKAGES) Console.SWITCH_PRO else Console.WIIMOTE_NUNCHUK
                onOpenMapping(MappingRoute(console, fromDsu = true))
            },
            onOpenSystemSettings = { startActivity(viewModel.permissionHandler.buildSettingsIntent()) },
            onOpenSettings = onOpenSettings,
            shizukuAvailable = shizukuAvailable,
            viewMode = viewMode,
        )
    }
}
