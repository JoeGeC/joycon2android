package com.joegec.joycon2android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.joegec.joycon2android.dsu.presentation.DsuViewModel
import com.joegec.joycon2android.gamepad.presentation.GamepadViewModel
import com.joegec.joycon2android.ui.Joycon2ViewModel
import com.joegec.joycon2android.ui.JoyconScreen
import com.joegec.joycon2android.dsu.presentation.DsuCardState
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
                    dolphinInstalled = c.emulatorSetup.dolphinInstalled,
                    configureDolphin = c.emulatorSetup::configureDolphinDsu,
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

    override fun onResume() {
        super.onResume()
        viewModel.recheckPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
                    val state by viewModel.uiState.collectAsState()
                    val gamepadStatus by gamepadViewModel.status.collectAsState()
                    val shizukuAvailable by gamepadViewModel.shizukuAvailable.collectAsState()
                    val dsuStatus by dsuViewModel.status.collectAsState()
                    val dolphinPhase by dsuViewModel.dolphinPhase.collectAsState()
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
                        dsuViewModel.resetDolphinPhase()
                        gamepadViewModel.resetSetupPhase()
                    }

                    JoyconScreen(
                        state = state,
                        gamepadEnabled = gamepadStatus.enabled,
                        gamepadError = gamepadStatus.error,
                        dsuState = DsuCardState(
                            enabled = dsuStatus.enabled,
                            error = dsuStatus.error,
                            clientCount = dsuStatus.clientCount,
                            address = dsuStatus.address,
                            showSlotLimitNote = state.activePlayers.any { it.player.index > 4 },
                            dolphinInstalled = dsuViewModel.dolphinInstalled,
                            dolphinAutoConfigAvailable = shizukuAvailable,
                            dolphinPhase = dolphinPhase,
                        ),
                        permissionDenied = permissionDenied,
                        onScan = { permLauncher.launch(permissionHandler.requiredPermissions) },
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
                        gamepadSetupAvailable = shizukuAvailable,
                        gamepadSetupPhase = gamepadSetupPhase,
                        onConfigureGamepad = { gamepadViewModel.configureGamepad(state.activePlayers) },
                        onDsuToggle = dsuViewModel::toggle,
                        onConfigureDolphin = { dsuViewModel.configureDolphinDsu(state.activePlayers) },
                        onOpenSettings = { startActivity(permissionHandler.buildSettingsIntent()) },
                        shizukuAvailable = shizukuAvailable,
                        viewMode = viewMode,
                        onViewModeChange = viewModel::setViewMode,
                    )
                }
            }
        }
    }
}
