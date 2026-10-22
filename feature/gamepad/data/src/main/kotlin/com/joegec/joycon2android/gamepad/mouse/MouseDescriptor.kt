package com.joegec.joycon2android.gamepad.mouse

import com.joegec.joycon2android.gamepad.UhidDevice

object MouseDescriptor {

    // Its own product, so nothing that finds the pads by vendor and product takes it for one.
    private const val PRODUCT_ID = 0x5679

    val device = UhidDevice(
        name = "Joy-Con Virtual Mouse",
        phys = "joycon2android/mouse",
        uniq = "mouse",
        productId = PRODUCT_ID,
        descriptor = byteArrayOf(
            0x05, 0x01,               // Usage Page (Generic Desktop)
            0x09, 0x02,               // Usage (Mouse)
            0xA1.toByte(), 0x01,      // Collection (Application)
            0x09, 0x01,               //   Usage (Pointer)
            0xA1.toByte(), 0x00,      //   Collection (Physical)

            0x05, 0x09,               //     Usage Page (Button)
            0x19, 0x01,               //     Usage Minimum (Button 1)
            0x29, 0x02,               //     Usage Maximum (Button 2)
            0x15, 0x00,               //     Logical Minimum (0)
            0x25, 0x01,               //     Logical Maximum (1)
            0x75, 0x01,               //     Report Size (1)
            0x95.toByte(), 0x02,      //     Report Count (2)
            0x81.toByte(), 0x02,      //     Input (Data, Var, Abs)
            0x75, 0x06,               //     Report Size (6) - padding
            0x95.toByte(), 0x01,      //     Report Count (1)
            0x81.toByte(), 0x03,      //     Input (Const, Var, Abs)

            0x05, 0x01,               //     Usage Page (Generic Desktop)
            0x09, 0x30,               //     Usage (X)
            0x09, 0x31,               //     Usage (Y)
            0x16, 0x01, 0x80.toByte(), //    Logical Minimum (-32767)
            0x26, 0xFF.toByte(), 0x7F, //    Logical Maximum (32767)
            0x75, 0x10,               //     Report Size (16)
            0x95.toByte(), 0x02,      //     Report Count (2)
            0x81.toByte(), 0x06,      //     Input (Data, Var, Rel)

            0x09, 0x38,               //     Usage (Wheel)
            0x15, 0x81.toByte(),      //     Logical Minimum (-127)
            0x25, 0x7F,               //     Logical Maximum (127)
            0x75, 0x08,               //     Report Size (8)
            0x95.toByte(), 0x01,      //     Report Count (1)
            0x81.toByte(), 0x06,      //     Input (Data, Var, Rel)

            0xC0.toByte(),            //   End Collection
            0xC0.toByte(),            // End Collection
        ),
    )
}
