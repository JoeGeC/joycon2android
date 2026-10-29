package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.model.PlayerState

/** Tells the player their press was mapped, on the controller in their hands. */
class ConfirmPressUseCase(private val repository: PressFeedbackRepository) {
    operator fun invoke(player: PlayerState) = repository.confirmPress(player)
}
