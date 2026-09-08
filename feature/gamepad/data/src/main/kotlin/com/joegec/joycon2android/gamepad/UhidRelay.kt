package com.joegec.joycon2android.gamepad
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell
import com.joegec.joycon2android.gamepad.privileged.ShellProcess

import android.content.Context
import android.util.Log
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class UhidRelay(private val name: String, private val playerIndex: Int) {

    private var process: ShellProcess? = null
    private var outputStream: OutputStream? = null

    // Pre-allocated buffers for sendReport (called at ~60Hz)
    private val inputHeader = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
    private val inputEventBuf = ByteBuffer.allocate(4 + 2 + REPORT_SIZE).order(ByteOrder.LITTLE_ENDIAN)

    fun create(context: Context, shell: PrivilegedShell): Boolean {
        return try {
            val relayPath = deployRelay(context, shell)
            val remoteProcess = shell.newProcess(arrayOf(relayPath))
                ?: run {
                    Log.e(TAG, "Privileged shell returned no process")
                    return false
                }
            process = remoteProcess
            val os = remoteProcess.outputStream

            // Wait for "OK\n" readiness signal from relay
            val inputStream = remoteProcess.inputStream
            val buf = ByteArray(3)
            var read = 0
            while (read < 3) {
                val n = inputStream.read(buf, read, 3 - read)
                if (n <= 0) {
                    Log.e(TAG, "Relay process closed before signalling readiness")
                    return false
                }
                read += n
            }

            // Send UHID_CREATE2 event
            val createEvent = buildCreateEvent()
            val header = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
            header.putInt(createEvent.size)
            os.write(header.array())
            os.write(createEvent)
            os.flush()

            outputStream = os
            Log.i(TAG, "UHID relay started for $name $playerIndex")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start UHID relay", e)
            destroy()
            false
        }
    }

    fun sendReport(report: ByteArray): Boolean {
        val os = outputStream ?: return false
        return try {
            inputHeader.clear()
            inputHeader.putInt(4 + 2 + report.size)

            inputEventBuf.clear()
            inputEventBuf.putInt(UHID_INPUT2)
            inputEventBuf.putShort(report.size.toShort())
            inputEventBuf.put(report)

            os.write(inputHeader.array(), 0, 4)
            os.write(inputEventBuf.array(), 0, 4 + 2 + report.size)
            true
        } catch (e: IOException) {
            Log.e(TAG, "Failed to send report", e)
            false
        }
    }

    fun destroy() {
        try {
            outputStream?.let { os ->
                try {
                    // Send shutdown signal (length = 0)
                    val shutdown = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN)
                    shutdown.putInt(0)
                    os.write(shutdown.array())
                    os.flush()
                } catch (_: IOException) {}
                os.close()
            }
        } catch (_: IOException) {}
        outputStream = null
        process?.destroy()
        process = null
        Log.i(TAG, "UHID relay stopped for $name $playerIndex")
    }

    private fun buildCreateEvent(): ByteArray {
        val buf = ByteBuffer.allocate(UHID_EVENT_SIZE).order(ByteOrder.LITTLE_ENDIAN)

        buf.putInt(UHID_CREATE2)

        // name[128]
        val nameBytes = "$name $playerIndex".toByteArray(Charsets.UTF_8)
        buf.put(nameBytes, 0, minOf(nameBytes.size, 127))
        buf.position(4 + 128)

        // phys[64]
        val physBytes = "joycon2android/$playerIndex".toByteArray(Charsets.UTF_8)
        buf.put(physBytes, 0, minOf(physBytes.size, 63))
        buf.position(4 + 128 + 64)

        // uniq[64]
        val uniqBytes = "player-$playerIndex".toByteArray(Charsets.UTF_8)
        buf.put(uniqBytes, 0, minOf(uniqBytes.size, 63))
        buf.position(4 + 128 + 64 + 64)

        buf.putShort(RDESC.size.toShort())
        buf.putShort(BUS_USB.toShort())
        buf.putInt(VENDOR_VIRTUAL)
        buf.putInt(PRODUCT_VIRTUAL)
        buf.putInt(1)       // version
        buf.putInt(0)       // country

        buf.put(RDESC)

        return buf.array()
    }

    companion object {
        private const val TAG = "UhidRelay"
        private const val RELAY_REMOTE_PATH = "/data/local/tmp/.uhid_relay"

        @Volatile
        private var relayDeployed = false

        private fun deployRelay(context: Context, shell: PrivilegedShell): String {
            if (relayDeployed) return RELAY_REMOTE_PATH

            val localPath = context.applicationInfo.nativeLibraryDir + "/libuhid_relay.so"

            // Copy from app's native lib dir to /data/local/tmp/ (shell-accessible)
            shell.newProcess(
                arrayOf("sh", "-c", "cp $localPath $RELAY_REMOTE_PATH && chmod 755 $RELAY_REMOTE_PATH"),
            )?.waitFor()
            relayDeployed = true
            return RELAY_REMOTE_PATH
        }

        private const val UHID_CREATE2 = 11
        private const val UHID_INPUT2 = 12
        private const val BUS_USB = 3
        private const val UHID_EVENT_SIZE = 4380
        private const val REPORT_SIZE = 14

        // Avoid Nintendo VID/PID — the hid-nintendo kernel driver intercepts those
        // and fails to initialize (since this isn't a real Joy-Con).
        private const val VENDOR_VIRTUAL = 0x1234
        private const val PRODUCT_VIRTUAL = 0x5678

        private val RDESC = byteArrayOf(
            0x05, 0x01,               // Usage Page (Generic Desktop)
            0x09, 0x05,               // Usage (Game Pad)
            0xA1.toByte(), 0x01,      // Collection (Application)

            // Buttons 1-15, the most a Game Pad collection can spend: Linux maps Button n to
            // BTN_GAMEPAD + n - 1, and that range ends at BTN_THUMBR (Button 15). A 16th would land
            // on 0x13F, which no Android key layout names, so it would reach no app at all.
            // ReportMapper picks which Joy-Con button takes which bit so that each lands on its
            // same-named Android keycode.
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

            // Left Trigger. Brake is the left one and Accelerator the right one, never the other
            // way round: Android aliases AXIS_LTRIGGER to AXIS_BRAKE and AXIS_RTRIGGER to AXIS_GAS,
            // and firmware that re-publishes a pad (AYN's Odin/Thor) synthesises its L2/R2 buttons
            // from those axes — inverted, it hands the emulator an L2 press for a ZR pull.
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

            // The Switch 2 controllers have 17 buttons, two more than one Game Pad collection can
            // carry. A second application collection outside the gamepad usages takes the overflow:
            // Linux falls back to BTN_MISC + n - 1 for a Button usage whose application is neither
            // pointer, joystick nor gamepad, and every Android key layout names that range
            // BUTTON_1..BUTTON_16. Vendor-defined so nothing tries to interpret the collection.
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
        )
    }
}
