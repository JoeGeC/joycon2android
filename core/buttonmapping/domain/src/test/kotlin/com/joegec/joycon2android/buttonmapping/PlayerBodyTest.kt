package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerBodyTest {

    private fun controller(model: ControllerModel) =
        ConnectedJoycon(address = model.name, side = Side.PRO, model = model, deviceName = "Joy-Con 2")

    @Test
    fun `a gamecube controller maps as its own body, a pro controller as a pair`() {
        val gameCube = controller(ControllerModel.GAMECUBE)
        val pro = controller(ControllerModel.PRO_CONTROLLER)

        assertEquals(JoyconSide.GAMECUBE, PlayerState(PlayerNumber.P1, left = gameCube, right = gameCube).joyconSide())
        assertEquals(JoyconSide.DUAL, PlayerState(PlayerNumber.P1, left = pro, right = pro).joyconSide())
    }

    @Test
    fun `a gamecube controller's ZL and Z are reported on the stick clicks, a pair's are not`() {
        assertEquals(JoyconButton.RS, JoyconButton.ZR.emittedFor(JoyconSide.GAMECUBE))
        assertEquals(JoyconButton.LS, JoyconButton.ZL.emittedFor(JoyconSide.GAMECUBE))
        assertEquals(JoyconButton.ZR, JoyconButton.ZR.emittedFor(JoyconSide.DUAL))
    }
}
