package com.joegec.joycon2android.gamepad

class UinputDevice(
    val name: String,
    val phys: String,
    val vendorId: Int,
    val productId: Int,
    val version: Int,
    val keys: List<Int>,
    val axes: List<AbsAxis>,
    /** Zero for a device without rumble. */
    val rumbleEffects: Int,
)

class AbsAxis(
    val code: Int,
    val min: Int,
    val max: Int,
    val fuzz: Int = 0,
    val flat: Int = 0,
    val resolution: Int = 0,
)
