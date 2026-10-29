package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerState

interface PressFeedbackRepository {
    fun confirmPress(player: PlayerState)
}
