package com.joegec.joycon2android.model

data class Joycon2State(
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val scanning: Boolean = false,
    val side: Side = Side.UNKNOWN,
    val error: String? = null,
    val foundDeviceName: String? = null,
    val packetId: Int = 0,
    val buttons: Long = 0,
    val pressed: Set<String> = emptySet(),
    val leftStickX: Int = 2048,
    val leftStickY: Int = 2048,
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
