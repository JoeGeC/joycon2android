package com.joegec.joycon2android.buttonmapping

import kotlin.math.roundToInt

/** Snapped to the step, so a dragged value can still match a layout. Overrides it until one is next applied. */
class SetSensitivityUseCase(private val repository: SensitivityRepository) {
    suspend operator fun invoke(console: Console, body: PlayerBody, percent: Int) =
        repository.set(console, body, snapped(percent))

    private fun snapped(percent: Int): Int {
        val step = RemoteMotion.SENSITIVITY_PERCENT_STEP
        val stepped = (percent.toFloat() / step).roundToInt() * step
        return stepped.coerceIn(RemoteMotion.SENSITIVITY_PERCENT_RANGE)
    }
}
