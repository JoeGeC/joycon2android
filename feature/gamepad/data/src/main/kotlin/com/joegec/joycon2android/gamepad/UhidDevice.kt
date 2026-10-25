package com.joegec.joycon2android.gamepad

class UhidDevice(
    val name: String,
    val phys: String,
    val uniq: String,
    val productId: Int,
    val descriptor: ByteArray,
)
