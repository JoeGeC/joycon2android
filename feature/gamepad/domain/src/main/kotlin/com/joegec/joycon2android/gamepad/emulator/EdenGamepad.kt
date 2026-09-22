package com.joegec.joycon2android.gamepad.emulator

/**
 * How Eden addresses one of our virtual gamepads: the `port` it assigns while enumerating input
 * devices, and the `guid` it derives from the device's USB ids — product then vendor, each a
 * 16-digit hex half. Both read from the live device, never assumed:
 * docs/virtual-gamepad.md#device-identity.
 */
data class EdenGamepad(val port: Int, val guid: String) {
    companion object {
        fun of(port: Int, vendorId: Int, productId: Int) =
            EdenGamepad(port, "%016x%016x".format(productId, vendorId))
    }
}
