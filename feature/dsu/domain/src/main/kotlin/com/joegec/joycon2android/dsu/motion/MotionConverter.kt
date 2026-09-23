package com.joegec.joycon2android.dsu.motion

import com.joegec.joycon2android.model.JoyconInput

/** Frames, scales and signs: docs/dsu-motion.md#motion-frame. Verify changes against Dolphin's pointer. */
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
