package com.joegec.joycon2android

import com.joegec.joycon2android.buttonmapping.presentation.ControllerMappingViewModel
import com.joegec.joycon2android.output.presentation.OutputViewModel

fun AppContainer.controllerMappingViewModel() = ControllerMappingViewModel(
    observeGlobalMapping,
    observeSavedLayouts,
    applyMappingLayout,
    applyGlobalLayout,
    setControllerMapping,
    setWiiController,
    setThrust,
    setSensitivity,
    setRumbleStrength,
    setProController,
    setDeviceKeysPaused,
    confirmPress,
    saveCustomLayout,
    saveGlobalLayout,
    deleteCustomLayout,
    deleteGlobalLayout,
)

fun AppContainer.outputViewModel() = OutputViewModel(
    observePlaying,
    observeOutputOn,
    setPlaying,
    selectEmulator,
    setMotion,
    setPad,
    emulators = emulatorTargets,
    configure = ::configureEmulator,
)
