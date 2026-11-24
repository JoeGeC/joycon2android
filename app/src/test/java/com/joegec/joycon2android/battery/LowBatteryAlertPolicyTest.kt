package com.joegec.joycon2android.battery

import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.BatteryCharge
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import com.joegec.joycon2android.settings.LowBatteryAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class LowBatteryAlertPolicyTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val session = MutableStateFlow(AppUiState())
    private val alert = MutableStateFlow(LowBatteryAlert(notify = true, vibrate = true, firstWarningPercent = 20, finalWarningPercent = 20))
    private val warnings = mutableListOf<ControllerBattery>()

    init {
        LowBatteryAlertPolicy(scope, session, alert, warn = { battery, _ -> warnings += battery }).start()
    }

    @After
    fun tearDown() = scope.cancel()

    private fun joycon(percent: Int, model: ControllerModel = ControllerModel.JOYCON_RIGHT) = ConnectedJoycon(
        address = "R",
        side = Side.RIGHT,
        deviceName = "Joy-Con 2 (R)",
        model = model,
        input = JoyconInput(battery = BatteryCharge(percent)),
    )

    private fun playerTwoHolding(percent: Int) = AppUiState(
        players = PlayerNumber.entries.map {
            if (it == PlayerNumber.P2) PlayerState(it, right = joycon(percent)) else PlayerState(it)
        },
    )

    @Test
    fun `a low controller is named with the player holding it`() {
        session.value = playerTwoHolding(15)

        assertEquals(
            listOf(ControllerBattery("R", PlayerNumber.P2, ControllerModel.JOYCON_RIGHT, "Joy-Con 2 (R)", 15)),
            warnings,
        )
    }

    @Test
    fun `an unassigned low controller has no player`() {
        session.value = AppUiState(unassignedJoycons = listOf(joycon(10)))

        assertEquals(listOf<PlayerNumber?>(null), warnings.map { it.player })
    }

    @Test
    fun `a packet that keeps the charge does not warn again`() {
        session.value = playerTwoHolding(15)
        session.value = playerTwoHolding(15).copy(scanning = true)

        assertEquals(1, warnings.size)
    }

    @Test
    fun `with notification and vibration both off it stays quiet`() {
        alert.value = alert.value.copy(notify = false, vibrate = false)
        session.value = playerTwoHolding(15)

        assertEquals(emptyList<ControllerBattery>(), warnings)
    }

    @Test
    fun `turning a warning back on warns about a controller already low`() {
        alert.value = alert.value.copy(notify = false, vibrate = false)
        session.value = playerTwoHolding(15)
        alert.value = alert.value.copy(vibrate = true)

        assertEquals(1, warnings.size)
    }

    @Test
    fun `vibration alone still warns`() {
        alert.value = alert.value.copy(notify = false)
        session.value = playerTwoHolding(15)

        assertEquals(1, warnings.size)
    }

    @Test
    fun `a controller without a battery reading is ignored`() {
        session.value = AppUiState(unassignedJoycons = listOf(joycon(10).copy(input = JoyconInput())))

        assertEquals(emptyList<ControllerBattery>(), warnings)
    }
}
