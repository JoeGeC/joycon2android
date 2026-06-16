package com.joegec.joycon2android.dsu.motion

import com.joegec.joycon2android.model.JoyconInput

/**
 * Raw Joy-Con IMU → cemuhook/DS4 motion frame.
 *
 * Scale factors are the Switch 1 family values, verified on Joy-Con 2 hardware
 * (2026-06: at rest gravity reads exactly −1.00 g): accel ±8 g → 0.000244 g/LSB,
 * gyro ±2000 dps → 0.06103 dps/LSB.
 *
 * Measured Joy-Con (R) raw frame (tools/README.md workflow, 2026-06): X = controller's
 * left, Y = toward the tail, Z = out of the button face. The cemuhook wire frame,
 * anchored against Dolphin's on-screen Wii pointer (its complementary filter makes the
 * ACCELEROMETER the authority on sustained pitch — gyro signs alone can't be judged
 * from pointer direction): x = left, y = down through the controller, z = toward the
 * player. Flat at rest → accel (0,−1,0); nose up → accel z = −1 and gyro pitch −
 * (verified via pointer flicks: gyro shows up in the fast response, accel in the
 * settled position); turn right → +yaw; roll right → +roll. The axis signs are DS4
 * hardware history, not a consistent right-handed frame — verify any change against
 * the pointer itself, fast and slow movements separately.
 *
 * Deliberately no grip-dependent rotation: like a physical DS4, the stream always
 * reports the controller's body frame — grip handling belongs to the emulator
 * (e.g. Dolphin's "Sideways Wii Remote"). Left Joy-Con and Pro are assumed to share
 * this frame — unverified; recalibrate with tools/dsu_client if motion feels rotated.
 */
object MotionConverter {

    private const val ACCEL_G_PER_LSB = 0.000244140625f
    private const val GYRO_DPS_PER_LSB = 0.06103515625f

    fun convert(input: JoyconInput?): DsuMotion {
        if (input == null) return DsuMotion()
        return DsuMotion(
            accelX = input.accelX * ACCEL_G_PER_LSB,
            accelY = -input.accelZ * ACCEL_G_PER_LSB,
            accelZ = input.accelY * ACCEL_G_PER_LSB,
            gyroPitch = input.gyroX * GYRO_DPS_PER_LSB,
            gyroYaw = -input.gyroZ * GYRO_DPS_PER_LSB,
            gyroRoll = -input.gyroY * GYRO_DPS_PER_LSB,
        )
    }
}
