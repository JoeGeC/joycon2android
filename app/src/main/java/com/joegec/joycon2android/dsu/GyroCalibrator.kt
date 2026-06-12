package com.joegec.joycon2android.dsu

import com.joegec.joycon2android.model.JoyconInput

/**
 * Removes per-controller gyro bias. Joy-Con 2 gyros idle with a constant offset
 * (+0.2 dps yaw / +0.9 dps roll observed on hardware), which DSU clients integrate
 * into a steady pointer drift. Whenever a controller's gyro stays within
 * [maxSpreadLsb] (~2.4 dps) for [windowSize] consecutive samples (~2 s at 120 Hz —
 * true at rest; hand tremor exceeds it), the window mean becomes that controller's
 * bias. Mirrors the runtime recalibration the Switch itself performs.
 */
class GyroCalibrator(
    private val windowSize: Int = 240,
    private val maxSpreadLsb: Int = 40,
) {

    private val windows = mutableMapOf<String, Window>()
    private val biases = mutableMapOf<String, IntArray>()

    fun calibrate(address: String, input: JoyconInput): JoyconInput {
        val window = windows.getOrPut(address) { Window() }
        window.add(input.gyroX, input.gyroY, input.gyroZ)
        if (window.count == windowSize) {
            if (window.spread() <= maxSpreadLsb) biases[address] = window.means()
            window.reset()
        }
        val bias = biases[address] ?: return input
        return input.copy(
            gyroX = input.gyroX - bias[0],
            gyroY = input.gyroY - bias[1],
            gyroZ = input.gyroZ - bias[2],
        )
    }

    private class Window {
        var count = 0
            private set
        private val sums = LongArray(3)
        private val mins = IntArray(3)
        private val maxs = IntArray(3)

        fun add(x: Int, y: Int, z: Int) {
            val sample = intArrayOf(x, y, z)
            for (axis in 0..2) {
                if (count == 0) {
                    mins[axis] = sample[axis]
                    maxs[axis] = sample[axis]
                } else {
                    mins[axis] = minOf(mins[axis], sample[axis])
                    maxs[axis] = maxOf(maxs[axis], sample[axis])
                }
                sums[axis] += sample[axis]
            }
            count++
        }

        fun spread(): Int = (0..2).maxOf { maxs[it] - mins[it] }

        fun means(): IntArray = IntArray(3) { (sums[it] / count).toInt() }

        fun reset() {
            count = 0
            sums.fill(0)
        }
    }
}
