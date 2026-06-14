package com.joegec.joycon2android

import android.content.Context
import com.joegec.joycon2android.ble.ControllerRepository
import com.joegec.joycon2android.ble.DisconnectControllerUseCase
import com.joegec.joycon2android.ble.Joycon2Manager
import com.joegec.joycon2android.ble.StartScanUseCase
import com.joegec.joycon2android.ble.StopScanUseCase
import com.joegec.joycon2android.domain.AssignmentRepository
import com.joegec.joycon2android.domain.ComboAssignmentDetector
import com.joegec.joycon2android.domain.PlayerAssignmentManager
import com.joegec.joycon2android.domain.PlayerStateResolver
import com.joegec.joycon2android.session.AssignControllerUseCase
import com.joegec.joycon2android.session.ObserveSessionUseCase
import com.joegec.joycon2android.session.SessionCoordinator
import com.joegec.joycon2android.session.UnassignControllerUseCase
import com.joegec.joycon2android.dsu.DisableDsuUseCase
import com.joegec.joycon2android.dsu.DsuRepository
import com.joegec.joycon2android.dsu.DsuServer
import com.joegec.joycon2android.dsu.EnableDsuUseCase
import com.joegec.joycon2android.dsu.ObserveDsuStatusUseCase
import com.joegec.joycon2android.dsu.PushDsuPadDataUseCase
import com.joegec.joycon2android.uhid.DisableGamepadUseCase
import com.joegec.joycon2android.uhid.EnableGamepadUseCase
import com.joegec.joycon2android.uhid.GamepadManager
import com.joegec.joycon2android.uhid.GamepadOutput
import com.joegec.joycon2android.uhid.GamepadRepository
import com.joegec.joycon2android.uhid.ObserveGamepadStatusUseCase
import com.joegec.joycon2android.uhid.ObserveWirelessDebugStatusUseCase
import com.joegec.joycon2android.uhid.OnPlayerAssignedUseCase
import com.joegec.joycon2android.uhid.OnPlayerUnassignedUseCase
import com.joegec.joycon2android.uhid.PrivilegedAccess
import com.joegec.joycon2android.uhid.PushGamepadStateUseCase
import com.joegec.joycon2android.uhid.StartPairingUseCase
import com.joegec.joycon2android.uhid.StartWirelessDiscoveryUseCase
import com.joegec.joycon2android.uhid.StopWirelessDiscoveryUseCase
import com.joegec.joycon2android.uhid.SubmitPairingCodeUseCase
import com.joegec.joycon2android.uhid.WirelessDebugRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Composition root: owns app-scoped repositories (data) and binds them to use cases
 * (domain). Presentation reaches data only through these use cases. Held by
 * [JoyconApplication] so the servers/connections outlive any single Activity or the
 * foreground service.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // --- Connection (BLE) ---
    val controllerRepository: ControllerRepository = Joycon2Manager(appContext, scope)
    val startScan = StartScanUseCase(controllerRepository)
    val stopScan = StopScanUseCase(controllerRepository)
    val disconnectController = DisconnectControllerUseCase(controllerRepository)

    // --- DSU ---
    private val dsuRepository: DsuRepository = DsuServer(scope)
    val enableDsu = EnableDsuUseCase(dsuRepository)
    val disableDsu = DisableDsuUseCase(dsuRepository)
    val pushDsuPadData = PushDsuPadDataUseCase(dsuRepository)
    val observeDsuStatus = ObserveDsuStatusUseCase(dsuRepository)

    // --- Assignment ---
    // Cross-feature orchestration that reacts to assignment (gamepad/DSU lifecycle) lives in
    // the SessionCoordinator below.
    val assignmentRepository: AssignmentRepository = PlayerAssignmentManager()

    // --- Gamepad + privileged access ---
    private val privilegedAccess = PrivilegedAccess(appContext, scope)
    private val gamepadRepository: GamepadRepository =
        GamepadOutput(scope, GamepadManager(scope, appContext), privilegedAccess::acquire)
    private val wirelessDebugRepository: WirelessDebugRepository = privilegedAccess

    init {
        // Connecting clears a stale "no privileged access" error; a revoked link kills the
        // relay socket, so drop the gamepad. Both are within the gamepad feature.
        privilegedAccess.onConnected = { gamepadRepository.clearError() }
        privilegedAccess.onConnectionLost = { gamepadRepository.disable() }
    }

    val enableGamepad = EnableGamepadUseCase(gamepadRepository)
    val disableGamepad = DisableGamepadUseCase(gamepadRepository)
    val pushGamepadState = PushGamepadStateUseCase(gamepadRepository)
    val onPlayerAssigned = OnPlayerAssignedUseCase(gamepadRepository)
    val onPlayerUnassigned = OnPlayerUnassignedUseCase(gamepadRepository)
    val observeGamepadStatus = ObserveGamepadStatusUseCase(gamepadRepository)

    val startWirelessDiscovery = StartWirelessDiscoveryUseCase(wirelessDebugRepository)
    val stopWirelessDiscovery = StopWirelessDiscoveryUseCase(wirelessDebugRepository)
    val startPairing = StartPairingUseCase(wirelessDebugRepository)
    val submitPairingCode = SubmitPairingCodeUseCase(wirelessDebugRepository)
    val observeWirelessDebugStatus = ObserveWirelessDebugStatusUseCase(wirelessDebugRepository)
    val shizukuAvailable: Boolean get() = wirelessDebugRepository.shizukuAvailable

    // --- Session (cross-feature coordinator) ---
    private val sessionCoordinator = SessionCoordinator(
        scope = scope,
        controllers = controllerRepository,
        assignments = assignmentRepository,
        resolver = PlayerStateResolver(evictConflicting = assignmentRepository::unassign),
        comboDetector = ComboAssignmentDetector(),
        onState = { state ->
            pushGamepadState(state.players)
            pushDsuPadData(state.activePlayers)
        },
        onPlayerAssigned = { onPlayerAssigned(it) },
        onPlayerUnassigned = { onPlayerUnassigned(it) },
    ).also { it.start() }

    val observeSession = ObserveSessionUseCase(sessionCoordinator)
    val assignController = AssignControllerUseCase(sessionCoordinator)
    val unassignController = UnassignControllerUseCase(sessionCoordinator)

    /** Cross-feature shutdown: stop every output and clear assignments/connections. */
    fun disconnectAll() {
        disableGamepad()
        disableDsu()
        assignmentRepository.unassignAll()
        controllerRepository.disconnectAll()
    }
}
