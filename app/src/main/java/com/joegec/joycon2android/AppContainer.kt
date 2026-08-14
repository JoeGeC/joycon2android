package com.joegec.joycon2android

import android.content.Context
import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.connection.DisconnectControllerUseCase
import com.joegec.joycon2android.connection.Joycon2Manager
import com.joegec.joycon2android.connection.ObserveViewModeUseCase
import com.joegec.joycon2android.connection.SetViewModeUseCase
import com.joegec.joycon2android.connection.StartScanUseCase
import com.joegec.joycon2android.connection.StopScanUseCase
import com.joegec.joycon2android.connection.ViewModePreferences
import com.joegec.joycon2android.connection.ViewModePreferencesDataStore
import com.joegec.joycon2android.buttonmapping.ControllerMappingDataStore
import com.joegec.joycon2android.buttonmapping.ControllerMappingRepository
import com.joegec.joycon2android.buttonmapping.GetEffectiveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ResetControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.SetControllerMappingUseCase
import com.joegec.joycon2android.assignment.AssignmentRepository
import com.joegec.joycon2android.assignment.ComboAssignmentDetector
import com.joegec.joycon2android.assignment.PlayerAssignmentManager
import com.joegec.joycon2android.assignment.PlayerStateResolver
import com.joegec.joycon2android.emulator.EmulatorSetup
import com.joegec.joycon2android.emulator.virtualGamepadPorts
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
import com.joegec.joycon2android.gamepad.DisableGamepadUseCase
import com.joegec.joycon2android.gamepad.EnableGamepadUseCase
import com.joegec.joycon2android.gamepad.GamepadManager
import com.joegec.joycon2android.gamepad.GamepadOutput
import com.joegec.joycon2android.gamepad.GamepadRepository
import com.joegec.joycon2android.gamepad.ObserveGamepadStatusUseCase
import com.joegec.joycon2android.gamepad.ObserveShizukuAvailabilityUseCase
import com.joegec.joycon2android.gamepad.OnPlayerAssignedUseCase
import com.joegec.joycon2android.gamepad.OnPlayerUnassignedUseCase
import com.joegec.joycon2android.gamepad.privileged.PrivilegedAccess
import com.joegec.joycon2android.gamepad.PushGamepadStateUseCase
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

    private val viewModePreferences: ViewModePreferences = ViewModePreferencesDataStore(appContext)
    val observeViewMode = ObserveViewModeUseCase(viewModePreferences)
    val setViewMode = SetViewModeUseCase(viewModePreferences)

    // --- Controller button mapping (shared by Gamepad and DSU) ---
    private val controllerMappingRepository: ControllerMappingRepository = ControllerMappingDataStore(appContext)
    val observeControllerMapping = ObserveControllerMappingUseCase(controllerMappingRepository)
    val setControllerMapping = SetControllerMappingUseCase(controllerMappingRepository)
    val resetControllerMapping = ResetControllerMappingUseCase(controllerMappingRepository)
    private val getControllerMapping = GetEffectiveControllerMappingUseCase(observeControllerMapping)

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
    private val privilegedAccess = PrivilegedAccess()
    private val gamepadRepository: GamepadRepository =
        GamepadOutput(scope, GamepadManager(scope, appContext), privilegedAccess::acquire)

    val enableGamepad = EnableGamepadUseCase(gamepadRepository)
    val disableGamepad = DisableGamepadUseCase(gamepadRepository)
    val pushGamepadState = PushGamepadStateUseCase(gamepadRepository)
    val onPlayerAssigned = OnPlayerAssignedUseCase(gamepadRepository)
    val onPlayerUnassigned = OnPlayerUnassignedUseCase(gamepadRepository)
    val observeGamepadStatus = ObserveGamepadStatusUseCase(gamepadRepository)

    val observeShizukuAvailability = ObserveShizukuAvailabilityUseCase(privilegedAccess)

    val emulatorSetup = EmulatorSetup(
        appContext.packageManager,
        privilegedAccess::acquire,
        scope = scope,
        gamepadPorts = { virtualGamepadPorts(appContext) },
        getControllerMapping = getControllerMapping,
    )

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
