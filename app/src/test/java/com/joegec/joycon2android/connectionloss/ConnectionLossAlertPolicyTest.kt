package com.joegec.joycon2android.connectionloss

import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.BatteryCharge
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconConnectionState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionLossAlertPolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val session = MutableStateFlow(AppUiState())
    private val notify = MutableStateFlow(true)
    private val shown = mutableListOf<LostController>()
    private val cleared = mutableListOf<String>()

    init {
        ConnectionLossAlertPolicy(scope, session, notify, show = { shown += it }, clear = { cleared += it }).start()
    }

    @After
    fun tearDown() = scope.cancel()

    private fun joycon(state: JoyconConnectionState) = ConnectedJoycon(
        address = "R",
        side = Side.RIGHT,
        deviceName = "Joy-Con 2 (R)",
        model = ControllerModel.JOYCON_RIGHT,
        connectionState = state,
    )

    private val connected = JoyconConnectionState(connected = true, ready = true)
    private fun lost(percent: Int?) =
        JoyconConnectionState(connectionLost = true, batteryAtLoss = percent?.let(::BatteryCharge))

    private fun playerTwoHolding(state: JoyconConnectionState) = AppUiState(
        players = PlayerNumber.entries.map {
            if (it == PlayerNumber.P2) PlayerState(it, right = joycon(state)) else PlayerState(it)
        },
    )

    @Test
    fun `a drop is named with the player holding it`() {
        session.value = playerTwoHolding(connected)
        session.value = playerTwoHolding(lost(60))

        assertEquals(
            listOf(LostController("R", PlayerNumber.P2, ControllerModel.JOYCON_RIGHT, "Joy-Con 2 (R)", false)),
            shown,
        )
    }

    @Test
    fun `a drop at one percent ran out of battery`() {
        session.value = playerTwoHolding(lost(1))

        assertEquals(listOf(true), shown.map { it.ranOutOfBattery })
    }

    @Test
    fun `a drop with no battery reading did not run out`() {
        session.value = AppUiState(unassignedJoycons = listOf(joycon(lost(null))))

        assertEquals(listOf(false), shown.map { it.ranOutOfBattery })
    }

    @Test
    fun `coming back clears the notification`() {
        session.value = playerTwoHolding(lost(60))
        session.value = playerTwoHolding(connected)

        assertEquals(listOf("R"), cleared)
    }

    @Test
    fun `with the notification off a drop stays quiet`() {
        notify.value = false
        session.value = playerTwoHolding(lost(60))

        assertEquals(emptyList<LostController>(), shown)
    }

    @Test
    fun `turning the notification on does not announce an earlier drop`() {
        notify.value = false
        session.value = playerTwoHolding(lost(60))
        notify.value = true

        assertEquals(emptyList<LostController>(), shown)
    }
}
