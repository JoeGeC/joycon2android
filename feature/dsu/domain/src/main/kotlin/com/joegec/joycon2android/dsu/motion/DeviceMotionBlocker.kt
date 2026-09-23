package com.joegec.joycon2android.dsu.motion

/** docs/dsu-motion.md#eden-reads-the-devices-own-motion */
interface DeviceMotionBlocker {
    suspend fun setBlocked(blocked: Boolean)
}
