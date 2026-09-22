package com.joegec.joycon2android.dsu.motion

import com.joegec.joycon2android.dsu.DsuStream
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side

/**
 * Turns a lone Joy-Con's IMU 90° about its button face into the grip it is held in, so an emulator
 * presenting it as a Pro Controller reads its tilt the way SDL delivers a real horizontal Joy-Con.
 * The direction was measured, and is the opposite of the stick's turn:
 * docs/dsu-motion.md#sideways-joy-cons.
 */
object SidewaysMotion {

    fun orient(stream: DsuStream, input: JoyconInput): JoyconInput {
        if (!stream.heldSideways) return input
        return when (stream.state.motionSource?.side) {
            Side.LEFT -> rotateLeft(input)
            Side.RIGHT -> rotateRight(input)
            else -> input
        }
    }

    private fun rotateLeft(input: JoyconInput) = input.copy(
        accelX = -input.accelY, accelY = input.accelX,
        gyroX = -input.gyroY, gyroY = input.gyroX,
    )

    private fun rotateRight(input: JoyconInput) = input.copy(
        accelX = input.accelY, accelY = -input.accelX,
        gyroX = input.gyroY, gyroY = -input.gyroX,
    )
}
