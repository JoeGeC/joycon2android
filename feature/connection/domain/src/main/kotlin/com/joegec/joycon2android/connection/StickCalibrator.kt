package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.JoyconInput

/** Onto 0..4095 centred on 2048, as everything downstream assumes: docs/protocol.md#stick-range-and-centre */
class StickCalibrator(
    restWindowSize: Int = DEFAULT_REST_WINDOW,
    maxRestSpreadLsb: Int = DEFAULT_MAX_REST_SPREAD,
    seedHalfSpan: Int = DEFAULT_SEED_HALF_SPAN,
) {

    private val leftX = Axis(restWindowSize, maxRestSpreadLsb, seedHalfSpan)
    private val leftY = Axis(restWindowSize, maxRestSpreadLsb, seedHalfSpan)
    private val rightX = Axis(restWindowSize, maxRestSpreadLsb, seedHalfSpan)
    private val rightY = Axis(restWindowSize, maxRestSpreadLsb, seedHalfSpan)

    fun calibrate(input: JoyconInput): JoyconInput = input.copy(
        stickX = leftX.rescale(input.stickX),
        stickY = leftY.rescale(input.stickY),
        rightStickX = rightX.rescale(input.rightStickX),
        rightStickY = rightY.rescale(input.rightStickY),
    )

    private class Axis(
        private val restWindowSize: Int,
        private val maxRestSpreadLsb: Int,
        seedHalfSpan: Int,
    ) {
        private var centre = CENTER
        private var centreLearned = false
        private var below = seedHalfSpan
        private var above = seedHalfSpan

        private var count = 0
        private var sum = 0L
        private var min = 0
        private var max = 0

        fun rescale(raw: Int): Int {
            learnCentre(raw)
            val delta = raw - centre
            val scaled = when {
                delta > 0 -> {
                    above = maxOf(above, delta)
                    CENTER + delta * CENTER / above
                }
                delta < 0 -> {
                    below = maxOf(below, -delta)
                    CENTER + delta * CENTER / below
                }
                else -> CENTER
            }
            return scaled.coerceIn(0, MAX)
        }

        private fun learnCentre(raw: Int) {
            if (centreLearned) return

            if (count == 0) {
                min = raw
                max = raw
            } else {
                min = minOf(min, raw)
                max = maxOf(max, raw)
            }
            sum += raw
            count++
            if (count < restWindowSize) return

            if (max - min <= maxRestSpreadLsb) {
                centre = (sum / count).toInt()
                centreLearned = true
            }
            count = 0
            sum = 0
        }
    }

    companion object {
        private const val CENTER = 2048
        private const val MAX = 4095

        // ~250 ms at the 120 Hz report rate: long enough that any deliberate stick movement
        // blows the spread test, short enough that centre lands before the first menu input.
        private const val DEFAULT_REST_WINDOW = 30
        private const val DEFAULT_MAX_REST_SPREAD = 32

        // Just under the smallest measured travel (~1180), so full tilt saturates early rather than short.
        private const val DEFAULT_SEED_HALF_SPAN = 1150
    }
}
