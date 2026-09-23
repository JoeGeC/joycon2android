package com.joegec.joycon2android.connection

/** Bonded-host MAC at bytes [10..15], zeroed while pairing: docs/protocol.md#advertising */
object JoyconAdvertisement {

    private const val HOST_MAC_OFFSET = 10
    private const val HOST_MAC_LENGTH = 6

    fun isPairing(manufacturerData: ByteArray): Boolean {
        if (manufacturerData.size < HOST_MAC_OFFSET + HOST_MAC_LENGTH) return true
        return (HOST_MAC_OFFSET until HOST_MAC_OFFSET + HOST_MAC_LENGTH)
            .all { manufacturerData[it] == 0.toByte() }
    }
}
