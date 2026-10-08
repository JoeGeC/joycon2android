package com.joegec.joycon2android.connection

/** Raw 12-bit units: the resting centre, then how far the stick travels above and below it per axis. */
data class StickCalibration(
    val centreX: Int,
    val centreY: Int,
    val aboveX: Int,
    val aboveY: Int,
    val belowX: Int,
    val belowY: Int,
)
