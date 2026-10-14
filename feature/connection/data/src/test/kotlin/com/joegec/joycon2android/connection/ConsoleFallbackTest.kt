package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConsoleFallbackTest {

    @Test
    fun `a controller with analog triggers never falls back to the console protocol`() {
        assertFalse(ConsoleFallback.allows(ControllerModel.GAMECUBE))
    }

    @Test
    fun `a Joy-Con or an unrecognised controller still can`() {
        assertTrue(ConsoleFallback.allows(ControllerModel.JOYCON_LEFT))
        assertTrue(ConsoleFallback.allows(ControllerModel.JOYCON_RIGHT))
        assertTrue(ConsoleFallback.allows(ControllerModel.UNKNOWN))
    }
}
