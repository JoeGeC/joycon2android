package com.joegec.joycon2android.buttonmapping

import kotlin.math.roundToInt

/** Snapped to the step, so a dragged value can still match a layout. */
class SetRumbleStrengthUseCase(private val repository: ControllerMappingRepository) {
    suspend operator fun invoke(console: Console, body: PlayerBody, percent: Int) {
        val snapped = ((percent.toFloat() / RUMBLE_PERCENT_STEP).roundToInt() * RUMBLE_PERCENT_STEP)
            .coerceIn(RUMBLE_PERCENT_RANGE)
        val stored = if (snapped == DEFAULT_RUMBLE_PERCENT) "" else snapped.toString()
        repository.set(console, body, RUMBLE_PERCENT_KEY, stored)
    }
}
