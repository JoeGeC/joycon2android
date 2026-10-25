package com.joegec.joycon2android.gamepad

object GamepadDescriptor {

    private const val PRODUCT_ID = 0x5678

    fun device(playerIndex: Int) = UhidDevice(
        name = "Joy-Con Virtual Gamepad $playerIndex",
        phys = "joycon2android/$playerIndex",
        uniq = "player-$playerIndex",
        productId = PRODUCT_ID,
        descriptor = DESCRIPTOR,
    )

    private val DESCRIPTOR = byteArrayOf(
        0x05, 0x01,               // Usage Page (Generic Desktop)
        0x09, 0x05,               // Usage (Game Pad)
        0xA1.toByte(), 0x01,      // Collection (Application)

        // Buttons 1-15, the most a Game Pad collection can spend before a button reaches no
        // app at all: docs/virtual-gamepad.md#buttons-and-keycodes.
        0x05, 0x09,               //   Usage Page (Button)
        0x19, 0x01,               //   Usage Minimum (Button 1)
        0x29, 0x0F,               //   Usage Maximum (Button 15)
        0x15, 0x00,               //   Logical Minimum (0)
        0x25, 0x01,               //   Logical Maximum (1)
        0x75, 0x01,               //   Report Size (1)
        0x95.toByte(), 0x0F,      //   Report Count (15)
        0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)
        0x75, 0x01,               //   Report Size (1) - padding
        0x95.toByte(), 0x01,      //   Report Count (1)
        0x81.toByte(), 0x03,      //   Input (Const, Var, Abs)

        // Hat Switch (D-pad)
        0x05, 0x01,               //   Usage Page (Generic Desktop)
        0x09, 0x39,               //   Usage (Hat switch)
        0x15, 0x00,               //   Logical Minimum (0)
        0x25, 0x07,               //   Logical Maximum (7)
        0x35, 0x00,               //   Physical Minimum (0)
        0x46, 0x3B, 0x01,         //   Physical Maximum (315)
        0x65, 0x14,               //   Unit (Degrees)
        0x75, 0x04,               //   Report Size (4)
        0x95.toByte(), 0x01,      //   Report Count (1)
        0x81.toByte(), 0x42,      //   Input (Data, Var, Abs, Null State)
        0x75, 0x04,               //   Report Size (4) - padding
        0x95.toByte(), 0x01,      //   Report Count (1)
        0x81.toByte(), 0x03,      //   Input (Const, Var, Abs)

        // Left Stick X
        0x05, 0x01,               //   Usage Page (Generic Desktop)
        0x09, 0x30,               //   Usage (X)
        0x16, 0x01, 0x80.toByte(), //  Logical Minimum (-32767)
        0x26, 0xFF.toByte(), 0x7F, //  Logical Maximum (32767)
        0x75, 0x10,               //   Report Size (16)
        0x95.toByte(), 0x01,      //   Report Count (1)
        0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)

        // Left Stick Y
        0x09, 0x31,               //   Usage (Y)
        0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)

        // Right Stick X (Z)
        0x09, 0x32,               //   Usage (Z)
        0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)

        // Right Stick Y (Rz)
        0x09, 0x35,               //   Usage (Rz)
        0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)

        // Left Trigger. Never swap Brake and Accelerator: docs/virtual-gamepad.md#buttons-and-keycodes
        0x05, 0x02,               //   Usage Page (Simulation Controls)
        0x09, 0xC5.toByte(),      //   Usage (Brake)
        0x15, 0x00,               //   Logical Minimum (0)
        0x26, 0xFF.toByte(), 0x00, //  Logical Maximum (255)
        0x75, 0x08,               //   Report Size (8)
        0x95.toByte(), 0x01,      //   Report Count (1)
        0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)

        // Right Trigger
        0x09, 0xC4.toByte(),      //   Usage (Accelerator)
        0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)

        0xC0.toByte(),            // End Collection

        // Overflow buttons GR and C: docs/virtual-gamepad.md#buttons-and-keycodes
        0x06, 0x00, 0xFF.toByte(), // Usage Page (Vendor Defined FF00)
        0x09, 0x01,               // Usage (Vendor 1)
        0xA1.toByte(), 0x01,      // Collection (Application)
        0x05, 0x09,               //   Usage Page (Button)
        0x19, 0x01,               //   Usage Minimum (Button 1)
        0x29, 0x02,               //   Usage Maximum (Button 2)
        0x15, 0x00,               //   Logical Minimum (0)
        0x25, 0x01,               //   Logical Maximum (1)
        0x75, 0x01,               //   Report Size (1)
        0x95.toByte(), 0x02,      //   Report Count (2)
        0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)
        0x75, 0x06,               //   Report Size (6) - padding
        0x95.toByte(), 0x01,      //   Report Count (1)
        0x81.toByte(), 0x03,      //   Input (Const, Var, Abs)
        0xC0.toByte(),            // End Collection

        // Android's Home and Back keys: docs/virtual-gamepad.md#android-keys
        0x05, 0x0C,               // Usage Page (Consumer)
        0x09, 0x01,               // Usage (Consumer Control)
        0xA1.toByte(), 0x01,      // Collection (Application)
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
    )
}
