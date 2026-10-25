package com.joegec.joycon2android.gamepad.keyboard

import com.joegec.joycon2android.gamepad.UhidDevice

object KeyboardDescriptor {

    private const val PRODUCT_ID = 0x567A

    const val KEYS_REPORT_ID: Byte = 1
    const val CONSUMER_REPORT_ID: Byte = 2

    // A letter hides the on-screen keyboard: docs/virtual-gamepad.md#keyboard
    val withLetters = device(firstUsage = 0x00)
    val withoutLetters = device(firstUsage = KeyboardUsages.FIRST_NON_LETTER)

    private fun device(firstUsage: Int) = UhidDevice(
        name = "Joy-Con Virtual Keyboard",
        phys = "joycon2android/keyboard",
        uniq = "keyboard",
        productId = PRODUCT_ID,
        descriptor = byteArrayOf(
            0x05, 0x01,               // Usage Page (Generic Desktop)
            0x09, 0x06,               // Usage (Keyboard)
            0xA1.toByte(), 0x01,      // Collection (Application)
            0x85.toByte(), KEYS_REPORT_ID,

            0x05, 0x07,               //   Usage Page (Keyboard)
            0x19, 0xE0.toByte(),      //   Usage Minimum (Left Control)
            0x29, 0xE7.toByte(),      //   Usage Maximum (Right GUI)
            0x15, 0x00,               //   Logical Minimum (0)
            0x25, 0x01,               //   Logical Maximum (1)
            0x75, 0x01,               //   Report Size (1)
            0x95.toByte(), 0x08,      //   Report Count (8)
            0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)
            0x75, 0x08,               //   Report Size (8) - reserved
            0x95.toByte(), 0x01,      //   Report Count (1)
            0x81.toByte(), 0x03,      //   Input (Const, Var, Abs)

            // An empty slot's 0 falls below a minimum past the letters, so it reads as no key.
            0x19, firstUsage.toByte(), //  Usage Minimum
            0x29, 0x65,               //   Usage Maximum (Application)
            0x15, firstUsage.toByte(), //  Logical Minimum
            0x25, 0x65,               //   Logical Maximum (101)
            0x75, 0x08,               //   Report Size (8)
            0x95.toByte(), 0x06,      //   Report Count (6)
            0x81.toByte(), 0x00,      //   Input (Data, Array, Abs)
            0xC0.toByte(),            // End Collection

            0x05, 0x0C,               // Usage Page (Consumer)
            0x09, 0x01,               // Usage (Consumer Control)
            0xA1.toByte(), 0x01,      // Collection (Application)
            0x85.toByte(), CONSUMER_REPORT_ID,
            0x0A, 0x23, 0x02,         //   Usage (AC Home)
            0x0A, 0x24, 0x02,         //   Usage (AC Back)
            0x15, 0x00,               //   Logical Minimum (0)
            0x25, 0x01,               //   Logical Maximum (1)
            0x75, 0x01,               //   Report Size (1)
            0x95.toByte(), 0x02,      //   Report Count (2)
            0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)
            0x75, 0x06,               //   Report Size (6) - padding
            0x95.toByte(), 0x01,      //   Report Count (1)
            0x81.toByte(), 0x03,      //   Input (Const, Var, Abs)
            0xC0.toByte(),            // End Collection
        ),
    )
}
