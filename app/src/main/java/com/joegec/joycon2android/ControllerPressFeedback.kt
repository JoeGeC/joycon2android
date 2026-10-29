package com.joegec.joycon2android

import com.joegec.joycon2android.buttonmapping.PressFeedbackRepository
import com.joegec.joycon2android.connection.ControllerRepository
import com.joegec.joycon2android.connection.VibrationSample
import com.joegec.joycon2android.model.PlayerState

class ControllerPressFeedback(private val controllers: ControllerRepository) : PressFeedbackRepository {

    override fun confirmPress(player: PlayerState) {
        listOfNotNull(player.left, player.right).forEach { controllers.vibrate(it.address, VibrationSample.BUZZ) }
    }
}
