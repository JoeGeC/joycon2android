package com.joegec.joycon2android.dsu.motion

/**
 * Stops emulators reading this device's own motion sensors. Eden's Android build feeds them into
 * Player 1 on top of any mapped motion, so a DSU controller's readings alternate with the device's.
 */
interface DeviceMotionBlocker {
    suspend fun setBlocked(blocked: Boolean)
}
