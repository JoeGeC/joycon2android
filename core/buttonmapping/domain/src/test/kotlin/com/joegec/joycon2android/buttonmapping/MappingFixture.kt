package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.first

/** The editor's use cases wired over in-memory stores, the way the composition root wires them. */
internal class MappingFixture(val console: Console = Console.WIIMOTE_NUNCHUK) {
    private val mappings = FakeControllerMappings()
    private val wiiControllers = FakeWiiControllers()
    private val thrusts = FakeThrusts()
    private val sensitivities = FakeSensitivities()
    private val savedLayouts = FakeSavedLayouts()
    private val globalLayouts = FakeGlobalLayouts()

    private val observeMapping = ObserveControllerMappingUseCase(mappings)
    private val observeMotion = ObserveRemoteMotionUseCase(
        ObserveWiiControllerUseCase(wiiControllers),
        ObserveThrustUseCase(thrusts),
        ObserveSensitivityUseCase(sensitivities),
    )
    private val applyPlayerMapping = ApplyPlayerMappingUseCase(mappings, wiiControllers, thrusts, sensitivities)

    val observePlayerMapping = ObservePlayerMappingUseCase(observeMapping, observeMotion, savedLayouts)
    val applyLayout = ApplyMappingLayoutUseCase(savedLayouts, applyPlayerMapping)
    val setMapping = SetControllerMappingUseCase(mappings)
    val setWiiController = SetWiiControllerUseCase(wiiControllers, observeMapping, mappings)
    val setThrust = SetThrustUseCase(thrusts)
    val setSensitivity = SetSensitivityUseCase(sensitivities)
    val saveCustomLayout = SaveCustomLayoutUseCase(savedLayouts, observePlayerMapping)
    val deleteCustomLayout = DeleteCustomLayoutUseCase(savedLayouts)
    val saveGlobalLayout = SaveGlobalLayoutUseCase(globalLayouts, observePlayerMapping)
    val applyGlobalLayout = ApplyGlobalLayoutUseCase(globalLayouts, savedLayouts, applyLayout, applyPlayerMapping)
    val observeGlobalMapping = ObserveGlobalMappingUseCase(observePlayerMapping, globalLayouts)
    val observeSavedLayouts = ObserveSavedLayoutsUseCase(savedLayouts)
    val setProController = SetProControllerUseCase(observeMapping, mappings)
    val observeSingleJoyconBodies = ObserveSingleJoyconBodiesUseCase(observeMapping)

    suspend fun playerMapping(body: PlayerBody) = observePlayerMapping(console, body).first()

    suspend fun globalMapping(vararg bodies: PlayerBody) = observeGlobalMapping(console, bodies.toList()).first()

    suspend fun savedLayoutNamed(name: String) =
        observeSavedLayouts(console).first().first { it.name == name }

    suspend fun layoutsFor(side: JoyconSide) =
        MappingLayouts.forBody(console, side, observeSavedLayouts(console).first())

    companion object {
        fun left(player: PlayerNumber = PlayerNumber.P1) = PlayerBody(player, JoyconSide.LEFT)
        fun right(player: PlayerNumber = PlayerNumber.P2) = PlayerBody(player, JoyconSide.RIGHT)
    }
}
