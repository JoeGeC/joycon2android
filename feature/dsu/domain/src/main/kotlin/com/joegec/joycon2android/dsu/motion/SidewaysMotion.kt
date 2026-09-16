package com.joegec.joycon2android.dsu.motion

import com.joegec.joycon2android.dsu.DsuStream
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side

/**
 * Turns a lone Joy-Con's IMU 90° about its button face into the grip it is held in sideways. The
 * pad's buttons and stick already arrive in that grip, so an emulator that presents it as a Pro
 * Controller (Eden) needs its motion there too, the way SDL delivers a real horizontal Joy-Con;
 * otherwise tilting reads as if the Joy-Con's nose pointed at the screen.
 *
 * Directions measured in Eden's Mario Kart 8 (2026-09), held up like a wheel: turning each body the
 * way `SidewaysMapper` turns its stick steered upside down on both Joy-Cons, so the IMU turns the
 * opposite way — its raw axes evidently don't line up with the stick's. Z (out of the face) is the
 * rotation axis, so it passes through unchanged.
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
