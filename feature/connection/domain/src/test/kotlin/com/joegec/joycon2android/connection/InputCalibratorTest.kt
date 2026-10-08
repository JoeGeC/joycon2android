package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InputCalibratorTest {

    @Test
    fun `a controller without analog triggers reports none`() {
        val calibrated = InputCalibrator(ControllerModel.PRO_CONTROLLER).calibrate(JoyconInput(triggerLeft = 120, triggerRight = 90))
        assertEquals(0, calibrated.triggerLeft)
        assertEquals(0, calibrated.triggerRight)
    }

    @Test
    fun `a gamecube c-stick reaches full tilt at its shorter travel`() {
        val calibrator = InputCalibrator(ControllerModel.GAMECUBE)
        assertEquals(4095, calibrator.calibrate(JoyconInput(rightStickY = 2048 + 1022)).rightStickY)
    }

    @Test
    fun `a pro controller's right stick keeps the joy-con seed`() {
        val calibrator = InputCalibrator(ControllerModel.PRO_CONTROLLER)
        assertTrue(calibrator.calibrate(JoyconInput(rightStickY = 2048 + 1022)).rightStickY < 4095)
    }

    @Test
    fun `a gamecube controller's triggers are calibrated`() {
        val calibrator = InputCalibrator(ControllerModel.GAMECUBE)
            .apply { useFactory(FactoryCalibration(triggerZeroLeft = 33, triggerZeroRight = 32)) }
        assertEquals(255, calibrator.calibrate(JoyconInput(triggerLeft = 180)).triggerLeft)
    }
}
