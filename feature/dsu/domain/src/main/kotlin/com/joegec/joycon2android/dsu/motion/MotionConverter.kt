package com.joegec.joycon2android.dsu.motion

import com.joegec.joycon2android.model.JoyconInput

/**
 * Raw Joy-Con IMU → cemuhook's DS4 motion frame. Both frames, the scale factors and the sign
 * history are in docs/dsu-motion.md#motion-frame — the signs are DS4 hardware convention rather
 * than a right-handed frame, so verify any change against Dolphin's pointer rather than reasoning
 * about it.
 *
 * Converts whatever frame it is given; a lone sideways Joy-Con is turned into its grip first by
 * [SidewaysMotion].
 */
object MotionConverter {

    private const val ACCEL_G_PER_LSB = 0.000244140625f
    private const val GYRO_DPS_PER_LSB = 0.06103515625f

    fun convert(input: JoyconInput?): DsuMotion {
        if (input == null) return DsuMotion()
        return DsuMotion(
            accelX = -input.accelX * ACCEL_G_PER_LSB,
            accelY = -input.accelZ * ACCEL_G_PER_LSB,
            accelZ = input.accelY * ACCEL_G_PER_LSB,
            gyroPitch = input.gyroX * GYRO_DPS_PER_LSB,
            gyroYaw = -input.gyroZ * GYRO_DPS_PER_LSB,
            gyroRoll = input.gyroY * GYRO_DPS_PER_LSB,
        )
    }
}
