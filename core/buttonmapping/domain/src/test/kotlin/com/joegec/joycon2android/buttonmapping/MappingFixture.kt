package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.first

/** The editor's use cases wired over in-memory stores, the way the composition root wires them. */
internal class MappingFixture(val console: Console = Console.WIIMOTE_NUNCHUK) {
    private val mappings = FakeControllerMappings()
    private val sidewaysRemotes = FakeSidewaysRemotes()
    private val savedLayouts = FakeSavedLayouts()
    private val globalLayouts = FakeGlobalLayouts()

    private val observeMapping = ObserveControllerMappingUseCase(mappings)
    private val observeSideways = ObserveSidewaysRemoteUseCase(sidewaysRemotes)
    private val applyPlayerMapping = ApplyPlayerMappingUseCase(mappings, sidewaysRemotes)

    val observePlayerMapping = ObservePlayerMappingUseCase(observeMapping, observeSideways, savedLayouts)
    val applyLayout = ApplyMappingLayoutUseCase(savedLayouts, applyPlayerMapping)
    val resetMapping = ResetControllerMappingUseCase(applyLayout)
    val setMapping = SetControllerMappingUseCase(mappings)
    val setSidewaysRemote = SetSidewaysRemoteUseCase(sidewaysRemotes)
    val saveCustomLayout = SaveCustomLayoutUseCase(savedLayouts, observePlayerMapping)
    val deleteCustomLayout = DeleteCustomLayoutUseCase(savedLayouts)
    val saveGlobalLayout = SaveGlobalLayoutUseCase(globalLayouts, observePlayerMapping)
    val applyGlobalLayout = ApplyGlobalLayoutUseCase(globalLayouts, applyLayout, applyPlayerMapping)
    val observeGlobalMapping = ObserveGlobalMappingUseCase(observePlayerMapping, globalLayouts)
    val observeSavedLayouts = ObserveSavedLayoutsUseCase(savedLayouts)

    suspend fun playerMapping(body: PlayerBody) = observePlayerMapping(console, body).first()

    suspend fun globalMapping(vararg bodies: PlayerBody) = observeGlobalMapping(console, bodies.toList()).first()

    suspend fun savedLayoutNamed(name: String) =
        observeSavedLayouts(console).first().first { it.displayName == name }

    suspend fun layoutsFor(side: JoyconSide) =
        MappingLayouts.forBody(console, side, observeSavedLayouts(console).first())

    companion object {
        fun left(player: PlayerNumber = PlayerNumber.P1) = PlayerBody(player, JoyconSide.LEFT)
        fun right(player: PlayerNumber = PlayerNumber.P2) = PlayerBody(player, JoyconSide.RIGHT)
    }
}
