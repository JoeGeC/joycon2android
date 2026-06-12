package com.joegec.joycon2android.dsu

/** Motion sample in the cemuhook/DS4 frame: accel in g, gyro in deg/s. */
data class DsuMotion(
    val accelX: Float = 0f,
    val accelY: Float = 0f,
    val accelZ: Float = 0f,
    val gyroPitch: Float = 0f,
    val gyroYaw: Float = 0f,
    val gyroRoll: Float = 0f,
)
