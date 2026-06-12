package com.joegec.joycon2android.ble

/**
 * Joy-Con 2 advertisements (manufacturer ID 0x0553) carry the bonded host's MAC at
 * bytes [10..15]: a button press wakes the controller to reconnect to that host and
 * advertises its address; holding SYNC (pairing mode) zeroes the field. Observed on
 * hardware 2026-06 — wake: `… 01 00 09 A7 9A 55 E2 98 0F …`, pairing:
 * `… 01 00 00 00 00 00 00 00 0F …`.
 */
object JoyconAdvertisement {

    private const val HOST_MAC_OFFSET = 10
    private const val HOST_MAC_LENGTH = 6

    /** True when the controller is open for pairing rather than waking for its bonded host. */
    fun isPairing(manufacturerData: ByteArray): Boolean {
        if (manufacturerData.size < HOST_MAC_OFFSET + HOST_MAC_LENGTH) return true
        return (HOST_MAC_OFFSET until HOST_MAC_OFFSET + HOST_MAC_LENGTH)
            .all { manufacturerData[it] == 0.toByte() }
    }
}
