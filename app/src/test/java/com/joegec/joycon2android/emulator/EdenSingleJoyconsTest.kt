package com.joegec.joycon2android.emulator

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EdenSingleJoyconsTest {

    private val singleJoycons = EdenSingleJoycons(MutableStateFlow(setOf(PlayerBody(PlayerNumber.P1, JoyconSide.LEFT))))

    private fun leftJoycon(player: PlayerNumber) =
        PlayerState(player, left = ConnectedJoycon(address = "L", side = Side.LEFT, deviceName = "Joy-Con"))

    @Test
    fun `a single Joy-Con body is marked on an output Eden reads`() {
        assertTrue(singleJoycons.markedFor(Console.SWITCH_PRO, leftJoycon(PlayerNumber.P1)).asSingleJoycon)
    }

    @Test
    fun `Dolphin's outputs keep the sideways grip`() {
        assertFalse(singleJoycons.markedFor(Console.GAMECUBE, leftJoycon(PlayerNumber.P1)).asSingleJoycon)
        assertFalse(singleJoycons.markedFor(Console.WIIMOTE_NUNCHUK, leftJoycon(PlayerNumber.P1)).asSingleJoycon)
    }

    @Test
    fun `a body set to Pro Controller stays sideways`() {
        assertFalse(singleJoycons.markedFor(Console.SWITCH_PRO, leftJoycon(PlayerNumber.P2)).asSingleJoycon)
    }
}
