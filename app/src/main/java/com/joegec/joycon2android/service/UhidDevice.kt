package com.joegec.joycon2android.service

import android.util.Log
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UhidDevice(private val name: String, private val playerIndex: Int) {

    private var stream: FileOutputStream? = null

    fun create(): Boolean {
        return try {
            val fos = FileOutputStream("/dev/uhid")
            stream = fos
            val event = buildCreateEvent()
            fos.write(event)
            fos.flush()
            Log.i(TAG, "UHID device created: $name $playerIndex")
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to create UHID device: ${e.message}")
            stream = null
            false
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied opening /dev/uhid: ${e.message}")
            stream = null
            false
        }
    }

    fun sendReport(report: ByteArray): Boolean {
        val fos = stream ?: return false
        return try {
            val event = buildInputEvent(report)
            fos.write(event)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to send report: ${e.message}")
            false
        }
    }

    fun destroy() {
        stream?.let { fos ->
            try {
                val event = buildDestroyEvent()
                fos.write(event)
                fos.close()
            } catch (_: IOException) {}
        }
        stream = null
        Log.i(TAG, "UHID device destroyed: $name $playerIndex")
    }

    private fun buildCreateEvent(): ByteArray {
        // struct uhid_event for UHID_CREATE2:
        // __u32 type (4 bytes) = UHID_CREATE2 (11)
        // struct uhid_create2_req:
        //   __u8 name[128]
        //   __u8 phys[64]
        //   __u8 uniq[64]
        //   __u16 rd_size
        //   __u16 bus
        //   __u32 vendor
        //   __u32 product
        //   __u32 version
        //   __u32 country
        //   __u8 rd_data[HID_MAX_DESCRIPTOR_SIZE=4096]
        // Total uhid_event size = 4380 bytes (padded struct)
        val buf = ByteBuffer.allocate(UHID_EVENT_SIZE).order(ByteOrder.LITTLE_ENDIAN)

        // type
        buf.putInt(UHID_CREATE2)

        // name[128]
        val nameBytes = "$name $playerIndex".toByteArray(Charsets.UTF_8)
        buf.put(nameBytes, 0, minOf(nameBytes.size, 127))
        buf.position(4 + 128)

        // phys[64]
        buf.position(4 + 128 + 64)

        // uniq[64]
        buf.position(4 + 128 + 64 + 64)

        // rd_size
        buf.putShort(RDESC.size.toShort())

        // bus
        buf.putShort(BUS_BLUETOOTH.toShort())

        // vendor
        buf.putInt(0x057E) // Nintendo

        // product
        buf.putInt(0x2009)

        // version
        buf.putInt(1)

        // country
        buf.putInt(0)

        // rd_data[4096]
        buf.put(RDESC)

        return buf.array()
    }

    private fun buildInputEvent(report: ByteArray): ByteArray {
        // struct uhid_event for UHID_INPUT2:
        // __u32 type (4 bytes) = UHID_INPUT2 (12)
        // struct uhid_input2_req:
        //   __u16 size
        //   __u8 data[4096]
        val buf = ByteBuffer.allocate(UHID_EVENT_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(UHID_INPUT2)
        buf.putShort(report.size.toShort())
        buf.put(report)
        return buf.array()
    }

    private fun buildDestroyEvent(): ByteArray {
        val buf = ByteBuffer.allocate(UHID_EVENT_SIZE).order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(UHID_DESTROY)
        return buf.array()
    }

    companion object {
        private const val TAG = "UhidDevice"

        private const val UHID_CREATE2 = 11
        private const val UHID_INPUT2 = 12
        private const val UHID_DESTROY = 1

        private const val BUS_BLUETOOTH = 5

        // uhid_event is a union — size is the largest member
        // On Linux: sizeof(struct uhid_event) = 4380
        private const val UHID_EVENT_SIZE = 4380

        // HID report descriptor: standard gamepad
        // 14 buttons + hat + 2x 16-bit sticks + 2x 8-bit triggers = 13 byte reports
        private val RDESC = byteArrayOf(
            0x05, 0x01,               // Usage Page (Generic Desktop)
            0x09, 0x05,               // Usage (Game Pad)
            0xA1.toByte(), 0x01,      // Collection (Application)

            // Buttons (14 buttons, 2 bits padding)
            0x05, 0x09,               //   Usage Page (Button)
            0x19, 0x01,               //   Usage Minimum (Button 1)
            0x29, 0x0E,               //   Usage Maximum (Button 14)
            0x15, 0x00,               //   Logical Minimum (0)
            0x25, 0x01,               //   Logical Maximum (1)
            0x75, 0x01,               //   Report Size (1)
            0x95.toByte(), 0x0E,      //   Report Count (14)
            0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)
            0x75, 0x01,               //   Report Size (1)
            0x95.toByte(), 0x02,      //   Report Count (2) - padding
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

            // Left Trigger
            0x05, 0x02,               //   Usage Page (Simulation Controls)
            0x09, 0xC4.toByte(),      //   Usage (Accelerator)
            0x15, 0x00,               //   Logical Minimum (0)
            0x26, 0xFF.toByte(), 0x00, //  Logical Maximum (255)
            0x75, 0x08,               //   Report Size (8)
            0x95.toByte(), 0x01,      //   Report Count (1)
            0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)

            // Right Trigger
            0x09, 0xC5.toByte(),      //   Usage (Brake)
            0x81.toByte(), 0x02,      //   Input (Data, Var, Abs)

            0xC0.toByte(),            // End Collection
        )
    }
}
