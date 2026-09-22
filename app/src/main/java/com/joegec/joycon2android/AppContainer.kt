package com.joegec.joycon2android

import android.content.Context
import com.joegec.joycon2android.connection.ConnectionPriorityRepository
import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.connection.DisconnectControllerUseCase
import com.joegec.joycon2android.connection.Joycon2Manager
import com.joegec.joycon2android.connection.ObserveViewModeUseCase
import com.joegec.joycon2android.connection.SetHighConnectionPriorityUseCase
import com.joegec.joycon2android.connection.SetViewModeUseCase
import com.joegec.joycon2android.connection.StartScanUseCase
import com.joegec.joycon2android.connection.StopScanUseCase
import com.joegec.joycon2android.connection.ViewModePreferences
import com.joegec.joycon2android.connection.ViewModePreferencesDataStore
import com.joegec.joycon2android.buttonmapping.ApplyGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.ApplyMappingLayoutUseCase
import com.joegec.joycon2android.buttonmapping.ControllerMappingDataStore
import com.joegec.joycon2android.buttonmapping.ControllerMappingRepository
import com.joegec.joycon2android.buttonmapping.DeleteCustomLayoutUseCase
import com.joegec.joycon2android.buttonmapping.DeleteGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.GetEffectiveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.GetSidewaysRemoteUseCase
import com.joegec.joycon2android.buttonmapping.GlobalLayoutDataStore
import com.joegec.joycon2android.buttonmapping.GlobalLayoutRepository
import com.joegec.joycon2android.buttonmapping.ApplyPlayerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveGlobalMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveSavedLayoutsUseCase
import com.joegec.joycon2android.buttonmapping.ObservePlayerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveSidewaysRemoteUseCase
import com.joegec.joycon2android.buttonmapping.ResetControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.SaveCustomLayoutUseCase
import com.joegec.joycon2android.buttonmapping.SaveGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.SavedLayoutDataStore
import com.joegec.joycon2android.buttonmapping.SavedLayoutRepository
import com.joegec.joycon2android.buttonmapping.SetControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.SetSidewaysRemoteUseCase
import com.joegec.joycon2android.buttonmapping.SidewaysRemoteDataStore
import com.joegec.joycon2android.buttonmapping.SidewaysRemoteRepository
import com.joegec.joycon2android.assignment.AssignmentRepository
import com.joegec.joycon2android.assignment.ComboAssignmentDetector
import com.joegec.joycon2android.assignment.PlayerAssignmentManager
import com.joegec.joycon2android.assignment.PlayerStateResolver
import com.joegec.joycon2android.emulator.EmulatorLauncher
import com.joegec.joycon2android.emulator.EmulatorSetup
import com.joegec.joycon2android.emulator.dolphinGamepadIds
import com.joegec.joycon2android.emulator.edenGamepads
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
import com.joegec.joycon2android.dsu.DsuMotionSettingsDataStore
import com.joegec.joycon2android.dsu.motion.DsuMotionSettingsRepository
import com.joegec.joycon2android.dsu.motion.ObserveDsuMotionSettingsUseCase
import com.joegec.joycon2android.dsu.motion.SetBlockDeviceMotionUseCase
import com.joegec.joycon2android.dsu.motion.SetDeviceMotionBlockedUseCase
import com.joegec.joycon2android.dsu.motion.SetFastMotionUseCase
import com.joegec.joycon2android.emulator.EdenDeviceMotionBlocker
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
import com.joegec.joycon2android.update.ApkDownloader
import com.joegec.joycon2android.update.ApkUpdateInstaller
import com.joegec.joycon2android.update.CheckForUpdateUseCase
import com.joegec.joycon2android.update.GitHubReleases
import com.joegec.joycon2android.update.InstallUpdateUseCase
import com.joegec.joycon2android.update.SkipUpdateUseCase
import com.joegec.joycon2android.update.SkippedVersionRepository
import com.joegec.joycon2android.update.SystemPackageInstaller
import com.joegec.joycon2android.update.UpdatePreferencesDataStore
import com.joegec.joycon2android.update.UpdateRepository
import com.joegec.joycon2android.update.installedAppVersion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import java.io.File

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
    private val joycon2Manager = Joycon2Manager(appContext, scope)
    val controllerRepository: ControllerRepository = joycon2Manager
    private val connectionPriorityRepository: ConnectionPriorityRepository = joycon2Manager
    private val setHighConnectionPriority = SetHighConnectionPriorityUseCase(connectionPriorityRepository)
    val startScan = StartScanUseCase(controllerRepository)
    val stopScan = StopScanUseCase(controllerRepository)
    val disconnectController = DisconnectControllerUseCase(controllerRepository)

    private val viewModePreferences: ViewModePreferences = ViewModePreferencesDataStore(appContext)
    val observeViewMode = ObserveViewModeUseCase(viewModePreferences)
    val setViewMode = SetViewModeUseCase(viewModePreferences)

    // --- Controller button mapping (shared by Gamepad and DSU) ---
    private val controllerMappingRepository: ControllerMappingRepository = ControllerMappingDataStore(appContext)
    private val savedLayoutRepository: SavedLayoutRepository = SavedLayoutDataStore(appContext)
    private val globalLayoutRepository: GlobalLayoutRepository = GlobalLayoutDataStore(appContext)
    private val sidewaysRemoteRepository: SidewaysRemoteRepository = SidewaysRemoteDataStore(appContext)

    private val observeControllerMapping = ObserveControllerMappingUseCase(controllerMappingRepository)
    private val observeSidewaysRemote = ObserveSidewaysRemoteUseCase(sidewaysRemoteRepository)
    private val observePlayerMapping =
        ObservePlayerMappingUseCase(observeControllerMapping, observeSidewaysRemote, savedLayoutRepository)
    private val applyPlayerMapping =
        ApplyPlayerMappingUseCase(controllerMappingRepository, sidewaysRemoteRepository)

    val observeGlobalMapping = ObserveGlobalMappingUseCase(observePlayerMapping, globalLayoutRepository)
    val observeSavedLayouts = ObserveSavedLayoutsUseCase(savedLayoutRepository)
    val setControllerMapping = SetControllerMappingUseCase(controllerMappingRepository)
    val setSidewaysRemote = SetSidewaysRemoteUseCase(sidewaysRemoteRepository)
    val applyMappingLayout = ApplyMappingLayoutUseCase(savedLayoutRepository, applyPlayerMapping)
    val resetControllerMapping = ResetControllerMappingUseCase(applyMappingLayout)
    val applyGlobalLayout =
        ApplyGlobalLayoutUseCase(globalLayoutRepository, applyMappingLayout, applyPlayerMapping)
    val saveCustomLayout = SaveCustomLayoutUseCase(savedLayoutRepository, observePlayerMapping)
    val saveGlobalLayout = SaveGlobalLayoutUseCase(globalLayoutRepository, observePlayerMapping)
    val deleteCustomLayout = DeleteCustomLayoutUseCase(savedLayoutRepository)
    val deleteGlobalLayout = DeleteGlobalLayoutUseCase(globalLayoutRepository)

    private val getControllerMapping = GetEffectiveControllerMappingUseCase(observeControllerMapping)
    private val getSidewaysRemote = GetSidewaysRemoteUseCase(observeSidewaysRemote)

    // --- DSU ---
    private val dsuRepository: DsuRepository = DsuServer(scope)
    val enableDsu = EnableDsuUseCase(dsuRepository)
    val disableDsu = DisableDsuUseCase(dsuRepository)
    val pushDsuPadData = PushDsuPadDataUseCase(dsuRepository)
    val observeDsuStatus = ObserveDsuStatusUseCase(dsuRepository)

    private val dsuMotionSettings: DsuMotionSettingsRepository = DsuMotionSettingsDataStore(appContext)
    val observeDsuMotionSettings = ObserveDsuMotionSettingsUseCase(dsuMotionSettings)
    val setFastMotion = SetFastMotionUseCase(dsuMotionSettings)
    val setBlockDeviceMotion = SetBlockDeviceMotionUseCase(dsuMotionSettings)

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
        gamepadDevices = { edenGamepads(appContext) },
        gamepadControllerNumbers = { dolphinGamepadIds(appContext) },
        getControllerMapping = getControllerMapping,
        getSidewaysRemote = getSidewaysRemote,
    )

    val emulatorLauncher = EmulatorLauncher(appContext)

    // --- Updates ---
    private val skippedVersions: SkippedVersionRepository = UpdatePreferencesDataStore(appContext)
    private val updateRepository: UpdateRepository = GitHubReleases(GITHUB_REPOSITORY)
    val checkForUpdate = CheckForUpdateUseCase(updateRepository, skippedVersions, installedAppVersion(appContext))
    val skipUpdate = SkipUpdateUseCase(skippedVersions)
    val installUpdate = InstallUpdateUseCase(
        ApkUpdateInstaller(
            downloadDirectory = File(appContext.cacheDir, UPDATE_CACHE_DIRECTORY),
            downloader = ApkDownloader(),
            systemInstaller = SystemPackageInstaller(appContext),
        )
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

    private val dsuMotionPolicy = DsuMotionPolicy(
        scope = scope,
        dsuEnabled = observeDsuStatus().map { it.enabled },
        settings = observeDsuMotionSettings(),
        privilegedShellAvailable = observeShizukuAvailability(),
        setHighConnectionPriority = setHighConnectionPriority,
        setDeviceMotionBlocked = SetDeviceMotionBlockedUseCase(EdenDeviceMotionBlocker(privilegedAccess::readyShell)),
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

    private companion object {
        const val GITHUB_REPOSITORY = "JoeGeC/joycon2android"
        const val UPDATE_CACHE_DIRECTORY = "updates"
    }
}
