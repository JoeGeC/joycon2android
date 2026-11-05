package com.joegec.joycon2android

import android.content.Context
import android.os.SystemClock
import com.joegec.joycon2android.connection.ConnectionPriorityRepository
import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.connection.ControllerTypePreferencesDataStore
import com.joegec.joycon2android.connection.DisconnectControllerUseCase
import com.joegec.joycon2android.connection.Joycon2Manager
import com.joegec.joycon2android.connection.ObserveViewModeUseCase
import com.joegec.joycon2android.connection.SetHighConnectionPriorityUseCase
import com.joegec.joycon2android.connection.SetViewModeUseCase
import com.joegec.joycon2android.connection.StartScanUseCase
import com.joegec.joycon2android.connection.StopScanUseCase
import com.joegec.joycon2android.connection.ViewModePreferences
import com.joegec.joycon2android.connection.ViewModePreferencesDataStore
import com.joegec.joycon2android.androidkeys.AndroidKeyRouter
import com.joegec.joycon2android.androidkeys.DeviceKeysPause
import com.joegec.joycon2android.androidkeys.HeldKeyRouter
import com.joegec.joycon2android.androidkeys.OutputBindings
import com.joegec.joycon2android.androidkeys.ShellAndroidKeyPresser
import com.joegec.joycon2android.buttonmapping.ApplyGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.ConfirmPressUseCase
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.KeyBindings
import com.joegec.joycon2android.buttonmapping.ObserveJoyconMouseUsersUseCase
import com.joegec.joycon2android.buttonmapping.ObserveKeyBindingsUseCase
import com.joegec.joycon2android.buttonmapping.androidKeyBindings
import com.joegec.joycon2android.buttonmapping.keyboardKeyBindings
import com.joegec.joycon2android.buttonmapping.mouseActionBindings
import com.joegec.joycon2android.buttonmapping.mouseModeCombos
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.buttonmapping.body
import com.joegec.joycon2android.buttonmapping.ApplyMappingLayoutUseCase
import com.joegec.joycon2android.buttonmapping.ControllerMappingDataStore
import com.joegec.joycon2android.buttonmapping.ControllerMappingRepository
import com.joegec.joycon2android.buttonmapping.DeleteCustomLayoutUseCase
import com.joegec.joycon2android.buttonmapping.DeleteGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.GetEffectiveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.GetRemoteMotionUseCase
import com.joegec.joycon2android.buttonmapping.GlobalLayoutDataStore
import com.joegec.joycon2android.buttonmapping.GlobalLayoutRepository
import com.joegec.joycon2android.buttonmapping.ApplyPlayerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveGlobalMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveSavedLayoutsUseCase
import com.joegec.joycon2android.buttonmapping.ObservePlayerMappingUseCase
import com.joegec.joycon2android.buttonmapping.ObserveRemoteMotionUseCase
import com.joegec.joycon2android.buttonmapping.ObserveRumbleStrengthsUseCase
import com.joegec.joycon2android.buttonmapping.ObserveSensitivityUseCase
import com.joegec.joycon2android.buttonmapping.ObserveWiiControllerUseCase
import com.joegec.joycon2android.buttonmapping.ObserveThrustUseCase
import com.joegec.joycon2android.buttonmapping.SaveCustomLayoutUseCase
import com.joegec.joycon2android.buttonmapping.SaveGlobalLayoutUseCase
import com.joegec.joycon2android.buttonmapping.SavedLayoutDataStore
import com.joegec.joycon2android.buttonmapping.SavedLayoutRepository
import com.joegec.joycon2android.buttonmapping.SetControllerMappingUseCase
import com.joegec.joycon2android.buttonmapping.SensitivityDataStore
import com.joegec.joycon2android.buttonmapping.ObserveSingleJoyconBodiesUseCase
import com.joegec.joycon2android.buttonmapping.SetProControllerUseCase
import com.joegec.joycon2android.buttonmapping.SensitivityRepository
import com.joegec.joycon2android.buttonmapping.SetRumbleStrengthUseCase
import com.joegec.joycon2android.buttonmapping.SetSensitivityUseCase
import com.joegec.joycon2android.buttonmapping.SetDeviceKeysPausedUseCase
import com.joegec.joycon2android.buttonmapping.SetWiiControllerUseCase
import com.joegec.joycon2android.buttonmapping.SetThrustUseCase
import com.joegec.joycon2android.buttonmapping.WiiControllerDataStore
import com.joegec.joycon2android.buttonmapping.WiiControllerRepository
import com.joegec.joycon2android.buttonmapping.ThrustDataStore
import com.joegec.joycon2android.buttonmapping.ThrustRepository
import com.joegec.joycon2android.assignment.AssignmentRepository
import com.joegec.joycon2android.assignment.ComboAssignmentDetector
import com.joegec.joycon2android.assignment.PlayerAssignmentManager
import com.joegec.joycon2android.assignment.PlayerStateResolver
import com.joegec.joycon2android.emulator.DeviceScreens
import com.joegec.joycon2android.emulator.EdenSingleJoycons
import com.joegec.joycon2android.emulator.EmulatorLauncher
import com.joegec.joycon2android.emulator.EmulatorSetup
import com.joegec.joycon2android.emulator.dolphinGamepadIds
import com.joegec.joycon2android.emulator.edenGamepads
import com.joegec.joycon2android.session.AssignControllerUseCase
import com.joegec.joycon2android.session.ObserveSessionUseCase
import com.joegec.joycon2android.session.SessionCoordinator
import com.joegec.joycon2android.session.SetControllerTypeUseCase
import com.joegec.joycon2android.session.UnassignControllerUseCase
import com.joegec.joycon2android.dsu.DisableDsuUseCase
import com.joegec.joycon2android.dsu.DsuRepository
import com.joegec.joycon2android.dsu.DsuServer
import com.joegec.joycon2android.dsu.EnableDsuUseCase
import com.joegec.joycon2android.dsu.ObserveDsuStatusUseCase
import com.joegec.joycon2android.dsu.PushDsuPadDataUseCase
import com.joegec.joycon2android.settings.MainScreenLaunchDataStore
import com.joegec.joycon2android.settings.MainScreenLaunchRepository
import com.joegec.joycon2android.settings.ObserveLaunchOnMainScreenUseCase
import com.joegec.joycon2android.settings.ObserveOutputSettingsUseCase
import com.joegec.joycon2android.settings.ObserveShowTipsUseCase
import com.joegec.joycon2android.settings.OutputSettingsDataStore
import com.joegec.joycon2android.settings.OutputSettingsRepository
import com.joegec.joycon2android.settings.SetBlockDeviceMotionUseCase
import com.joegec.joycon2android.settings.SetDeviceMotionBlockedUseCase
import com.joegec.joycon2android.settings.SetFasterUpdatesUseCase
import com.joegec.joycon2android.settings.SetLaunchOnMainScreenUseCase
import com.joegec.joycon2android.settings.SetShowTipsUseCase
import com.joegec.joycon2android.settings.TipsDataStore
import com.joegec.joycon2android.settings.TipsRepository
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
import com.joegec.joycon2android.gamepad.keyboard.KeyboardOutput
import com.joegec.joycon2android.gamepad.keyboard.ObserveKeyboardEnabledUseCase
import com.joegec.joycon2android.gamepad.keyboard.KeyboardRepository
import com.joegec.joycon2android.gamepad.keyboard.DeclareKeyboardKeysUseCase
import com.joegec.joycon2android.gamepad.keyboard.PushKeyboardFrameUseCase
import com.joegec.joycon2android.gamepad.keyboard.SetKeyboardEnabledUseCase
import com.joegec.joycon2android.gamepad.mouse.MappedMouse
import com.joegec.joycon2android.gamepad.mouse.MouseOutput
import com.joegec.joycon2android.gamepad.mouse.MouseRepository
import com.joegec.joycon2android.gamepad.mouse.ObserveMouseEnabledUseCase
import com.joegec.joycon2android.gamepad.mouse.PushMouseFrameUseCase
import com.joegec.joycon2android.gamepad.mouse.SetMouseEnabledUseCase
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.mouse.MouseMix
import com.joegec.joycon2android.mouse.MouseModeToggle
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.output.DeviceKeyKind
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.ObserveOutputOnUseCase
import com.joegec.joycon2android.output.ObservePlayingUseCase
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputPlan
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.output.PlayingDataStore
import com.joegec.joycon2android.output.PlayingRepository
import com.joegec.joycon2android.output.PlayingSelection
import com.joegec.joycon2android.output.SelectEmulatorUseCase
import com.joegec.joycon2android.output.SetMotionUseCase
import com.joegec.joycon2android.output.SetOutputOnUseCase
import com.joegec.joycon2android.output.SetPlayingUseCase
import com.joegec.joycon2android.output.StartRememberedOutputsUseCase
import com.joegec.joycon2android.output.StopOutputsUseCase
import com.joegec.joycon2android.mouse.MouseRouter
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

/** Composition root: docs/architecture.md#composition-root--appcontainer */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // --- Connection (BLE) ---
    private val joycon2Manager = Joycon2Manager(appContext, scope, ControllerTypePreferencesDataStore(appContext))
    val controllerRepository: ControllerRepository = joycon2Manager
    private val connectionPriorityRepository: ConnectionPriorityRepository = joycon2Manager
    private val setHighConnectionPriority = SetHighConnectionPriorityUseCase(connectionPriorityRepository)
    val startScan = StartScanUseCase(controllerRepository)
    val stopScan = StopScanUseCase(controllerRepository)
    val disconnectController = DisconnectControllerUseCase(controllerRepository)

    private val viewModePreferences: ViewModePreferences = ViewModePreferencesDataStore(appContext)
    val observeViewMode = ObserveViewModeUseCase(viewModePreferences)
    val setViewMode = SetViewModeUseCase(viewModePreferences)

    private val tipsRepository: TipsRepository = TipsDataStore(appContext)
    val observeShowTips = ObserveShowTipsUseCase(tipsRepository)
    val setShowTips = SetShowTipsUseCase(tipsRepository)

    private val mainScreenLaunchRepository: MainScreenLaunchRepository = MainScreenLaunchDataStore(appContext)
    val observeLaunchOnMainScreen = ObserveLaunchOnMainScreenUseCase(mainScreenLaunchRepository)
    val setLaunchOnMainScreen = SetLaunchOnMainScreenUseCase(mainScreenLaunchRepository)

    // --- Controller button mapping (shared by Gamepad and DSU) ---
    private val controllerMappingRepository: ControllerMappingRepository = ControllerMappingDataStore(appContext)
    private val savedLayoutRepository: SavedLayoutRepository = SavedLayoutDataStore(appContext)
    private val globalLayoutRepository: GlobalLayoutRepository = GlobalLayoutDataStore(appContext)
    private val wiiControllerRepository: WiiControllerRepository = WiiControllerDataStore(appContext)
    private val thrustRepository: ThrustRepository = ThrustDataStore(appContext)
    private val sensitivityRepository: SensitivityRepository = SensitivityDataStore(appContext)

    private val observeControllerMapping = ObserveControllerMappingUseCase(controllerMappingRepository)
    private val observeRumbleStrengths = ObserveRumbleStrengthsUseCase(observeControllerMapping)
    private val observeWiiController = ObserveWiiControllerUseCase(wiiControllerRepository)
    private val observeRemoteMotion = ObserveRemoteMotionUseCase(
        observeWiiController,
        ObserveThrustUseCase(thrustRepository),
        ObserveSensitivityUseCase(sensitivityRepository),
    )
    private val observePlayerMapping =
        ObservePlayerMappingUseCase(observeControllerMapping, observeRemoteMotion, savedLayoutRepository)
    private val applyPlayerMapping =
        ApplyPlayerMappingUseCase(
            controllerMappingRepository,
            wiiControllerRepository,
            thrustRepository,
            sensitivityRepository,
        )

    val observeGlobalMapping = ObserveGlobalMappingUseCase(observePlayerMapping, globalLayoutRepository)
    val observeSavedLayouts = ObserveSavedLayoutsUseCase(savedLayoutRepository)
    val setControllerMapping = SetControllerMappingUseCase(controllerMappingRepository)
    val setWiiController =
        SetWiiControllerUseCase(wiiControllerRepository, observeControllerMapping, controllerMappingRepository)
    val setThrust = SetThrustUseCase(thrustRepository)
    val setSensitivity = SetSensitivityUseCase(sensitivityRepository)
    val setRumbleStrength = SetRumbleStrengthUseCase(controllerMappingRepository)
    val setProController = SetProControllerUseCase(observeControllerMapping, controllerMappingRepository)
    private val edenSingleJoycons = EdenSingleJoycons(
        ObserveSingleJoyconBodiesUseCase(observeControllerMapping)()
            .stateIn(scope, SharingStarted.Eagerly, ObserveSingleJoyconBodiesUseCase.LONE_BODIES.toSet()),
    )
    val applyMappingLayout = ApplyMappingLayoutUseCase(savedLayoutRepository, applyPlayerMapping)
    val applyGlobalLayout =
        ApplyGlobalLayoutUseCase(globalLayoutRepository, savedLayoutRepository, applyMappingLayout, applyPlayerMapping)
    val saveCustomLayout = SaveCustomLayoutUseCase(savedLayoutRepository, observePlayerMapping)
    val saveGlobalLayout = SaveGlobalLayoutUseCase(globalLayoutRepository, observePlayerMapping)
    val deleteCustomLayout = DeleteCustomLayoutUseCase(savedLayoutRepository)
    val deleteGlobalLayout = DeleteGlobalLayoutUseCase(globalLayoutRepository)

    private val getControllerMapping = GetEffectiveControllerMappingUseCase(observeControllerMapping)
    private val getRemoteMotion = GetRemoteMotionUseCase(observeRemoteMotion)

    // --- DSU ---
    private val dsuRepository: DsuRepository = DsuServer(scope)
    private val enableDsu = EnableDsuUseCase(dsuRepository)
    private val disableDsu = DisableDsuUseCase(dsuRepository)
    val pushDsuPadData = PushDsuPadDataUseCase(dsuRepository)
    val observeDsuStatus = ObserveDsuStatusUseCase(dsuRepository)


    // --- Settings (apply to whichever output runs) ---
    private val outputSettings: OutputSettingsRepository = OutputSettingsDataStore(appContext)
    val observeOutputSettings = ObserveOutputSettingsUseCase(outputSettings)
    val setFasterUpdates = SetFasterUpdatesUseCase(outputSettings)
    val setBlockDeviceMotion = SetBlockDeviceMotionUseCase(outputSettings)

    // --- Assignment ---
    val assignmentRepository: AssignmentRepository = PlayerAssignmentManager()

    // --- Gamepad + privileged access ---
    private val privilegedAccess = PrivilegedAccess()
    private val gamepadRepository: GamepadRepository =
        GamepadOutput(
            scope,
            GamepadManager(
                scope,
                appContext,
                ControllerRumble(controllerRepository, { activePlayers }, { rumbleStrengths.value })::play,
            ),
            privilegedAccess::acquire,
        )

    private val enableGamepad = EnableGamepadUseCase(gamepadRepository)
    private val disableGamepad = DisableGamepadUseCase(gamepadRepository)
    val pushGamepadState = PushGamepadStateUseCase(gamepadRepository)
    val onPlayerAssigned = OnPlayerAssignedUseCase(gamepadRepository)
    val onPlayerUnassigned = OnPlayerUnassignedUseCase(gamepadRepository)
    val observeGamepadStatus = ObserveGamepadStatusUseCase(gamepadRepository)

    val observeShizukuAvailability = ObserveShizukuAvailabilityUseCase(privilegedAccess)

    // --- Mouse ---
    private val mouseRepository: MouseRepository = MouseOutput(scope, appContext, privilegedAccess::acquire)
    private val mouseMix = MouseMix(MappedMouse(SystemClock::uptimeMillis), PushMouseFrameUseCase(mouseRepository)::invoke)
    private val mouseRouter = MouseRouter(
        enabled = mouseRepository.enabled,
        mouseOff = { player -> player !in mouseUsers.value || player in mouseModeToggle.off.value },
        push = mouseMix::desk,
    )
    val observeMouseEnabled = ObserveMouseEnabledUseCase(mouseRepository)

    // --- Keyboard ---
    private val keyboardRepository: KeyboardRepository = KeyboardOutput(scope, appContext, privilegedAccess::acquire)
    val observeKeyboardEnabled = ObserveKeyboardEnabledUseCase(keyboardRepository)

    val emulatorSetup = EmulatorSetup(
        appContext.packageManager,
        privilegedAccess::acquire,
        scope = scope,
        gamepadDevices = { edenGamepads(appContext) },
        gamepadControllerNumbers = { dolphinGamepadIds(appContext) },
        getControllerMapping = getControllerMapping,
        getRemoteMotion = getRemoteMotion,
        markSingleJoycons = edenSingleJoycons::marked,
    )

    val emulatorLauncher = EmulatorLauncher(appContext)
    val deviceScreens = DeviceScreens(appContext)

    // --- Output: what's being played picks the devices and the console mapped: docs/architecture.md#output ---
    private val playingRepository: PlayingRepository = PlayingDataStore(appContext, emulatorSetup::tabTarget)
    val observePlaying = ObservePlayingUseCase(playingRepository)
    val observeOutputOn = ObserveOutputOnUseCase(playingRepository)
    val setOutputOn = SetOutputOnUseCase(playingRepository)
    val setPlaying = SetPlayingUseCase(playingRepository)
    val setMotion = SetMotionUseCase(playingRepository)
    val selectEmulator = SelectEmulatorUseCase(playingRepository)
    private val startRememberedOutputs = StartRememberedOutputsUseCase(playingRepository)
    private val stopAllOutputs = StopOutputsUseCase(playingRepository)
    val emulatorTargets: Map<Playing, List<EmulatorTarget>> by lazy { Playing.entries.associateWith(emulatorSetup::targets) }

    private val playing = observePlaying().stateIn(scope, SharingStarted.Eagerly, PlayingSelection())
    private val outputOn = observeOutputOn()
    private val console: StateFlow<Console> = playing.map { it.playing.console }
        .stateIn(scope, SharingStarted.Eagerly, PlayingSelection().playing.console)
    private val runningConsoles: StateFlow<Set<Console>> = combine(console, outputOn) { console, on ->
        if (on) setOf(console) else emptySet()
    }.stateIn(scope, SharingStarted.Eagerly, emptySet())

    /** The mapping editor opens on what's being played. */
    fun consoleInView(): Console = console.value

    suspend fun configureEmulator(target: EmulatorTarget, players: List<PlayerState>, closeEmulator: Boolean) =
        playing.value.let { emulatorSetup.configure(target, it.playing, it.motion, players, closeEmulator) }

    // --- Device keys: Android, keyboard and mouse, from every running tab's mapping ---
    private val activeBodies = MutableStateFlow(emptyList<PlayerBody>())
    private val observeAndroidKeyBindings = ObserveKeyBindingsUseCase(observeControllerMapping, observeWiiController) { entries, _, _ ->
        entries.androidKeyBindings()
    }
    private val observeKeyboardKeyBindings = ObserveKeyBindingsUseCase(observeControllerMapping, observeWiiController) { entries, side, sideways ->
        entries.keyboardKeyBindings(side, sideways)
    }
    private val observeMouseActionBindings = ObserveKeyBindingsUseCase(observeControllerMapping, observeWiiController) { entries, side, sideways ->
        entries.mouseActionBindings(side, sideways)
    }
    private val deviceKeysPause = DeviceKeysPause()
    val setDeviceKeysPaused = SetDeviceKeysPausedUseCase(deviceKeysPause)
    val confirmPress = ConfirmPressUseCase(ControllerPressFeedback(controllerRepository))
    private val deviceKeysActing = combine(outputOn, deviceKeysPause.paused) { on, paused -> on && !paused }.stateIn(scope, SharingStarted.Eagerly, false)
    private val androidBindings = bindingsFor(observeAndroidKeyBindings)
    private val keyboardBindings = bindingsFor(observeKeyboardKeyBindings)
    private val mouseBindings = bindingsFor(observeMouseActionBindings)
    private val observeJoyconMouseUsers = ObserveJoyconMouseUsersUseCase(observeControllerMapping)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val mouseUsers: StateFlow<Set<PlayerNumber>> = combine(runningConsoles, activeBodies, ::Pair)
        .flatMapLatest { (consoles, bodies) -> observeJoyconMouseUsers(consoles, bodies) }
        .stateIn(scope, SharingStarted.Eagerly, emptySet())
    private val observeMouseModeCombos = ObserveKeyBindingsUseCase(observeControllerMapping, observeWiiController) { entries, side, sideways ->
        entries.mouseModeCombos(side, sideways)
    }

    // Every layout binds a combo, so only a player using the mouse can toggle it.
    private val mouseModeCombos = combine(bindingsFor(observeMouseModeCombos), mouseUsers) { combos, users ->
        combos.filterKeys { it in users }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())
    private val mouseModeToggle = MouseModeToggle(
        OutputBindings(deviceKeysActing, mouseModeCombos),
        SystemClock::elapsedRealtime,
        confirmPress::invoke,
    )
    val mouseOffPlayers: StateFlow<Set<PlayerNumber>> = mouseModeToggle.off

    // A player with their mouse off clicks nothing, so the game gets those buttons back.
    private val mouseModeBindings = combine(mouseBindings, mouseUsers, mouseModeToggle.off) { bindings, users, off ->
        bindings.filterKeys { it in users && it !in off }
    }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rumbleStrengths: StateFlow<Map<PlayerNumber, Int>> = combine(console, activeBodies, ::Pair)
        .flatMapLatest { (console, bodies) -> observeRumbleStrengths(console, bodies) }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    private val androidKeyRouter = AndroidKeyRouter(
        bindings = OutputBindings(deviceKeysActing, androidBindings),
        padEnabled = gamepadRepository.enabled,
        keyboardEnabled = keyboardRepository.enabled,
        pressKey = ShellAndroidKeyPresser(scope, privilegedAccess::readyShell)::press,
    )
    private val keyboardKeyRouter = HeldKeyRouter(OutputBindings(deviceKeysActing, keyboardBindings)) { held ->
        copy(keyboard = keyboard.copy(keys = held))
    }
    private val mouseActions = OutputBindings(deviceKeysActing, mouseModeBindings)
    private val mouseActionRouter = HeldKeyRouter(mouseActions) { held -> copy(mouseActions = held) }
    private val pushKeyboardFrame = PushKeyboardFrameUseCase(keyboardRepository)

    val mappedKeys: StateFlow<Set<DeviceKeyKind>> =
        combine(androidBindings, keyboardBindings, mouseUsers) { android, keyboard, mouseUsers ->
            buildSet {
                if (mouseUsers.isNotEmpty()) add(DeviceKeyKind.MOUSE)
                if (keyboard.anyBound()) add(DeviceKeyKind.KEYBOARD)
                if (android.anyBound()) add(DeviceKeyKind.ANDROID)
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptySet())

    // The keyboard is the Android keys' fast path, so they need it as keyboard keys do.
    private val mappedDevices = mappedKeys.map { kinds ->
        buildSet {
            if (DeviceKeyKind.ANDROID in kinds || DeviceKeyKind.KEYBOARD in kinds) add(OutputDevice.KEYBOARD)
            if (DeviceKeyKind.MOUSE in kinds) add(OutputDevice.MOUSE)
        }
    }
    val plannedDevices: StateFlow<Set<OutputDevice>> =
        combine(playing, outputOn, observeShizukuAvailability(), mappedDevices) { selection, on, shizuku, mapped ->
            if (on) OutputPlan.devices(selection.playing, selection.motion, shizuku, mapped) else emptySet()
        }.stateIn(scope, SharingStarted.Eagerly, emptySet())

    private fun Map<PlayerNumber, KeyBindings<*>>.anyBound() = values.any { it.sources.isNotEmpty() }
    private val hasActivePlayers = MutableStateFlow(false)
    @Volatile private var activePlayers = emptyList<PlayerState>()

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <K> bindingsFor(observe: ObserveKeyBindingsUseCase<K>): StateFlow<Map<PlayerNumber, KeyBindings<K>>> =
        combine(runningConsoles, activeBodies, ::Pair)
            .flatMapLatest { (consoles, bodies) -> observe(consoles, bodies) }
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

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
            activeBodies.value = state.activePlayers.mapNotNull { it.body() }
            activePlayers = state.activePlayers
            hasActivePlayers.value = state.activePlayers.isNotEmpty()
            deviceKeysPause.onFrame(state.activePlayers)
            mouseModeToggle.onFrame(state.activePlayers)
            mouseRouter.route(state)
            val routed = mouseActionRouter.route(state, keyboardKeyRouter.route(state, androidKeyRouter.route(state)))
            pushKeyboardFrame(routed.keyboard)
            mouseMix.send(routed.mouseActions)
            pushGamepadState(
                routed.gamepad.map { it.copy(state = edenSingleJoycons.markedFor(console.value, it.state)) },
            )
            pushDsuPadData(routed.dsu.map { edenSingleJoycons.markedFor(console.value, it) })
        },
        onPlayerAssigned = { onPlayerAssigned(it) },
        onPlayerUnassigned = { onPlayerUnassigned(it) },
    ).also { it.start() }

    private val outputActive = combine(observeDsuStatus(), observeGamepadStatus()) { dsu, gamepad ->
        dsu.enabled || gamepad.enabled
    }

    private val outputAutoStartPolicy = OutputAutoStartPolicy(scope, hasActivePlayers, startRememberedOutputs::invoke)
        .also { it.start() }

    private val outputDevicesPolicy = OutputDevicesPolicy(
        scope = scope,
        devices = plannedDevices,
        hasPlayers = hasActivePlayers,
        privilegedShellAvailable = observeShizukuAvailability(),
        setPad = { on -> if (on) enableGamepad(activePlayers) else disableGamepad() },
        setMotion = { on -> if (on) enableDsu() else disableDsu() },
    ).also { it.start() }

    private val fasterUpdatesPolicy = FasterUpdatesPolicy(
        scope = scope,
        outputActive = outputOn,
        fasterUpdates = observeOutputSettings().map { it.fasterUpdates },
        setHighConnectionPriority = setHighConnectionPriority,
    ).also { it.start() }

    private val deviceMotionBlockPolicy = DeviceMotionBlockPolicy(
        scope = scope,
        outputActive = outputActive,
        blockDeviceMotion = observeOutputSettings().map { it.blockDeviceMotion },
        privilegedShellAvailable = observeShizukuAvailability(),
        setDeviceMotionBlocked = SetDeviceMotionBlockedUseCase(EdenDeviceMotionBlocker(privilegedAccess::readyShell)),
    ).also { it.start() }

    private val mousePolicy = PrivilegedDevicePolicy(
        scope = scope,
        setting = plannedDevices.map { OutputDevice.MOUSE in it },
        privilegedShellAvailable = observeShizukuAvailability(),
        setEnabled = SetMouseEnabledUseCase(mouseRepository)::invoke,
    ).also { it.start() }

    private val keyboardPolicy = PrivilegedDevicePolicy(
        scope = scope,
        setting = plannedDevices.map { OutputDevice.KEYBOARD in it },
        privilegedShellAvailable = observeShizukuAvailability(),
        setEnabled = SetKeyboardEnabledUseCase(keyboardRepository)::invoke,
    ).also { it.start() }

    private val keyboardDeclarationPolicy = KeyboardDeclarationPolicy(
        scope = scope,
        bound = keyboardBindings.map { bindings -> bindings.values.flatMapTo(mutableSetOf()) { it.sources.keys } },
        declare = DeclareKeyboardKeysUseCase(keyboardRepository)::invoke,
    ).also { it.start() }

    val observeSession = ObserveSessionUseCase(sessionCoordinator)
    val assignController = AssignControllerUseCase(sessionCoordinator)
    val unassignController = UnassignControllerUseCase(sessionCoordinator)
    val setControllerType = SetControllerTypeUseCase(sessionCoordinator)

    /** Switches the card off, and the pad and DSU with it at once, not when the policy next runs. */
    fun stopOutputs() {
        stopAllOutputs()
        disableGamepad()
        disableDsu()
    }

    fun disconnectAll() {
        stopOutputs()
        assignmentRepository.unassignAll()
        controllerRepository.disconnectAll()
    }

    private companion object {
        const val GITHUB_REPOSITORY = "JoeGeC/joycon2android"
        const val UPDATE_CACHE_DIRECTORY = "updates"
    }
}
