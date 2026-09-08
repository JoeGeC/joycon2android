package com.joegec.joycon2android.gamepad.emulator

/**
 * How Eden addresses one of our virtual gamepads: the `port` it assigns while enumerating input
 * devices, plus the `guid` it derives from the device's USB ids — product then vendor, each as a
 * 16-digit hex half.
 *
 * Both are read from the live input device, never assumed. Handhelds whose firmware re-publishes
 * external gamepads under the built-in controller's vendor/product (AYN's Odin/Thor line does this)
 * hand the emulator ids that are not the ones our uhid device was created with, and a binding whose
 * guid doesn't match the device Eden sees is silently ignored.
 */
data class EdenGamepad(val port: Int, val guid: String) {
    companion object {
        fun of(port: Int, vendorId: Int, productId: Int) =
            EdenGamepad(port, "%016x%016x".format(productId, vendorId))
    }
}
