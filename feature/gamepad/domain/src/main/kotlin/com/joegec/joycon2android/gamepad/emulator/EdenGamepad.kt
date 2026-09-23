package com.joegec.joycon2android.gamepad.emulator

/** Read from the live device, never assumed: docs/virtual-gamepad.md#device-identity */
data class EdenGamepad(val port: Int, val guid: String) {
    companion object {
        fun of(port: Int, vendorId: Int, productId: Int) =
            EdenGamepad(port, "%016x%016x".format(productId, vendorId))
    }
}
