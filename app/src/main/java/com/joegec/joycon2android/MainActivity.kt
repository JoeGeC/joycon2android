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
                    c.observeDsuMotionSettings,
                    c.setFastMotion,
                    c.setBlockDeviceMotion,
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
                    c.resetControllerMapping,
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
        // The UI is always dark, so force light bar icons rather than letting them follow the
        // device's light/dark mode (which would render dark-on-dark on a light-mode device).
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
                    var mappingConsole by rememberSaveable { mutableStateOf<Console?>(null) }

                    UpdatePrompt()

                    AnimatedContent(
                        targetState = mappingConsole,
                        transitionSpec = { pushTransition(forward = targetState != null) },
                        label = "mappingScreen",
                    ) { console ->
                        Box(Modifier.fillMaxSize().background(Background)) {
                            if (console != null) {
                                ControllerMappingRoute(console, onBack = { mappingConsole = null })
                            } else {
                                MainRoute(
                                    onScan = { permLauncher.launch(permissionHandler.requiredPermissions) },
                                    onOpenMapping = { mappingConsole = it },
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
    private fun ControllerMappingRoute(console: Console, onBack: () -> Unit) {
        val session by viewModel.uiState.collectAsState()
        val players = session.activePlayers
        val bodies = players.mapNotNull { it.body() }
        val state by controllerMappingViewModel.uiState.collectAsState()

        LaunchedEffect(console, bodies) { controllerMappingViewModel.edit(console, bodies) }

        state?.let {
            ControllerMappingScreen(
                state = it,
                players = players,
                actions = mappingActions,
                onBack = onBack,
            )
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
        resetMapping = { controllerMappingViewModel.resetMapping(it) },
        setSidewaysRemote = { body, enabled ->
            controllerMappingViewModel.setSidewaysRemoteEnabled(body, enabled)
        },
    )

    @Composable
    private fun MainRoute(onScan: () -> Unit, onOpenMapping: (Console) -> Unit) {
        val state by viewModel.uiState.collectAsState()
        val gamepadStatus by gamepadViewModel.status.collectAsState()
        val shizukuAvailable by gamepadViewModel.shizukuAvailable.collectAsState()
        val dsuStatus by dsuViewModel.status.collectAsState()
        val dsuSetupPhase by dsuViewModel.setupPhase.collectAsState()
        val selectedDsuEmulator by dsuViewModel.selectedEmulator.collectAsState()
        val dsuMotionSettings by dsuViewModel.motionSettings.collectAsState()
        val dsuEmulatorToClose by dsuViewModel.emulatorToClose.collectAsState()
        val gamepadEmulatorToClose by gamepadViewModel.emulatorToClose.collectAsState()
        val dsuEmulatorToStart by dsuViewModel.emulatorToStart.collectAsState()
        val gamepadEmulatorToStart by gamepadViewModel.emulatorToStart.collectAsState()
        val gamepadSetupPhase by gamepadViewModel.setupPhase.collectAsState()
        val selectedEmulator by gamepadViewModel.selectedEmulator.collectAsState()
        val permissionDenied by viewModel.permissionDenied.collectAsState()
        val viewMode by viewModel.viewMode.collectAsState()

        // A written emulator config is keyed to the current assignment; once it changes,
        // the Done/Failed state is stale, so reset both setup buttons.
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
                address = dsuStatus.address,
                coverage = DsuSlots.coverage(state.activePlayers),
                emulators = dsuViewModel.dsuEmulators,
                selectedEmulator = selectedDsuEmulator,
                setupPhase = dsuSetupPhase,
                motionSettings = dsuMotionSettings,
                deviceMotionBlockAvailable = shizukuAvailable,
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
                onOpenMapping(if (selectedEmulator in EdenPaths.PACKAGES) Console.SWITCH_PRO else Console.GAMECUBE)
            },
            onDsuToggle = dsuViewModel::toggle,
            onSelectDsuEmulator = dsuViewModel::selectEmulator,
            onConfigureDsu = { dsuViewModel.configureDsu(state.activePlayers) },
            onOpenDsuMapping = {
                onOpenMapping(if (selectedDsuEmulator in EdenPaths.PACKAGES) Console.SWITCH_PRO else Console.WIIMOTE_NUNCHUK)
            },
            onFastMotionToggle = dsuViewModel::toggleFastMotion,
            onBlockDeviceMotionToggle = dsuViewModel::toggleBlockDeviceMotion,
            onOpenSettings = { startActivity(viewModel.permissionHandler.buildSettingsIntent()) },
            shizukuAvailable = shizukuAvailable,
            viewMode = viewMode,
            onViewModeChange = viewModel::setViewMode,
        )
    }
}
