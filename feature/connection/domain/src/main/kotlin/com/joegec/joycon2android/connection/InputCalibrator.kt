package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.model.JoyconInput

class InputCalibrator(private val model: ControllerModel) {

    private val sticks =
        if (model == ControllerModel.GAMECUBE) StickCalibrator(rightSeedHalfSpan = C_STICK_SEED_HALF_SPAN)
        else StickCalibrator()
    private val triggers = TriggerCalibrator()

    fun useFactory(factory: FactoryCalibration) {
        sticks.useFactory(factory.mainStick, factory.rightStick)
        triggers.setFactoryZeros(factory.triggerZeroLeft, factory.triggerZeroRight)
    }

    // Bytes 0x3C/0x3D carry nothing meaningful on a controller without analog triggers.
    fun calibrate(input: JoyconInput): JoyconInput {
        val calibrated = sticks.calibrate(input)
        return if (model.hasAnalogTriggers) triggers.calibrate(calibrated)
        else calibrated.copy(triggerLeft = 0, triggerRight = 0)
    }

    private companion object {
        // Just under the shortest C-stick travel measured, 1022 (three units, 2026-10-07).
        const val C_STICK_SEED_HALF_SPAN = 1000
    }
}
