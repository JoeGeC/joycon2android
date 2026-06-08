package com.joegec.joycon2android.model

data class JoyconInput(
    val packetId: Int = 0,
    val buttons: Long = 0,
    val pressed: Set<String> = emptySet(),
    val stickX: Int = 2048,
    val stickY: Int = 2048,
    val rightStickX: Int = 2048,
    val rightStickY: Int = 2048,
    val accelX: Int = 0,
    val accelY: Int = 0,
    val accelZ: Int = 0,
    val gyroX: Int = 0,
    val gyroY: Int = 0,
    val gyroZ: Int = 0,
    val batteryVolts: Float = 0f,
)
