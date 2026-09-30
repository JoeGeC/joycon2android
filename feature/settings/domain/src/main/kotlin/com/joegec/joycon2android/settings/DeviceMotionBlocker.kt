package com.joegec.joycon2android.settings

/** docs/dsu-motion.md#eden-reads-the-devices-own-motion */
interface DeviceMotionBlocker {
    suspend fun setBlocked(blocked: Boolean)
}
