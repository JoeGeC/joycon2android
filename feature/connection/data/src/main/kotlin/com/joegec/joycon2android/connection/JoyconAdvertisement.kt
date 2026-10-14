package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel

/** Manufacturer data for Nintendo's ID: docs/protocol.md#advertising */
object JoyconAdvertisement {

    private const val PRODUCT_ID_LOW = 5
    private const val HOST_MAC_OFFSET = 10
    private const val HOST_MAC_LENGTH = 6

    fun model(manufacturerData: ByteArray): ControllerModel? {
        if (manufacturerData.size <= PRODUCT_ID_LOW) return null
        return when (manufacturerData[PRODUCT_ID_LOW].toInt() and 0xFF) {
            0x67 -> ControllerModel.JOYCON_LEFT
            0x66 -> ControllerModel.JOYCON_RIGHT
            0x69 -> ControllerModel.PRO_CONTROLLER
            0x73 -> ControllerModel.GAMECUBE
            else -> null
        }
    }

    // Bonded-host MAC, zeroed while pairing.
    fun isPairing(manufacturerData: ByteArray): Boolean {
        if (manufacturerData.size < HOST_MAC_OFFSET + HOST_MAC_LENGTH) return true
        return (HOST_MAC_OFFSET until HOST_MAC_OFFSET + HOST_MAC_LENGTH)
            .all { manufacturerData[it] == 0.toByte() }
    }
}
