package com.joegec.joycon2android

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.ArrayDeque
import java.util.UUID

/**
 * Joy-Con 2 BLE manager (single controller).
 *
 * Ported from the working macOS Joycon2BLEReceiver.mm. All UUIDs, init bytes,
 * field offsets and timings match that reference. See README.md.
 *
 * Usage:
 *   val mgr = Joycon2Manager(context) { state -> /* update UI */ }
 *   mgr.startScan()
 *   ...
 *   mgr.stop()
 */
class Joycon2Manager(
    private val context: Context,
    private val onState: (Joycon2State) -> Unit,
) {
    companion object {
        private const val TAG = "Joycon2"

        // Advertising manufacturer ID (Nintendo). Match this to find Joy-Cons.
        const val NINTENDO_MANUFACTURER_ID = 0x0553

        val INPUT_SERVICE: UUID = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd0")
        val NOTIFY_CHAR: UUID = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd2")
        val WRITE_CHAR: UUID = UUID.fromString("649d4ac9-8eb7-4e6c-af44-1ea54fe5f005")
        val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        // 12-byte init commands, written WITHOUT response.
        val INIT_CMD_1 = byteArrayOf(
            0x0c, 0x91.toByte(), 0x01, 0x02, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        ) // enable standard/button reports
        val INIT_CMD_2 = byteArrayOf(
            0x0c, 0x91.toByte(), 0x01, 0x04, 0x00, 0x04,
            0x00, 0x00, 0xFF.toByte(), 0x00, 0x00, 0x00
        ) // enable IMU / mouse / extended reports

        private const val DESIRED_MTU = 247
        private const val INIT_GAP_MS = 500L
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = btManager.adapter

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null
    private var side: Side = Side.UNKNOWN

    @Volatile private var scanning = false

    // ---- GATT operation queue ----------------------------------------------
    // Android allows only one outstanding GATT op at a time. We enqueue ops and
    // run the next one only after the matching callback fires.
    private val opQueue = ArrayDeque<() -> Unit>()
    private var opInFlight = false

    private fun enqueue(op: () -> Unit) {
        opQueue.add(op)
        maybeRunNext()
    }

    private fun maybeRunNext() {
        if (opInFlight) return
        val op = opQueue.poll() ?: return
        opInFlight = true
        op()
    }

    private fun opComplete() {
        opInFlight = false
        maybeRunNext()
    }

    // ---- Public API ---------------------------------------------------------

    @SuppressLint("MissingPermission")
    fun startScan() {
        val scanner = adapter?.bluetoothLeScanner ?: run {
            Log.e(TAG, "No BLE scanner (bluetooth off?)")
            return
        }
        scanning = true
        // We don't filter by service UUID in the scan filter because the input
        // service isn't always in the advertisement; we match on manufacturer
        // data in the callback instead.
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(null, settings, scanCallback)
        Log.i(TAG, "Scanning for Joy-Con 2 ...")
    }

    @SuppressLint("MissingPermission")
    fun stop() {
        scanning = false
        adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        opQueue.clear()
        opInFlight = false
    }

    // ---- Scanning -----------------------------------------------------------

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val record: ScanRecord = result.scanRecord ?: return
            val mfr = record.getManufacturerSpecificData(NINTENDO_MANUFACTURER_ID) ?: return
            // It's a Nintendo BLE controller. Stop scanning and connect.
            if (!scanning) return
            scanning = false
            adapter?.bluetoothLeScanner?.stopScan(this)

            val name = result.device.name ?: record.deviceName ?: ""
            side = detectSide(name)
            Log.i(TAG, "Found Joy-Con: '$name' side=$side mfrLen=${mfr.size}")
            onState(Joycon2State(connected = false, connecting = true, side = side))

            // TRANSPORT_LE is important — forces BLE, not classic.
            gatt = result.device.connectGatt(
                context, false, gattCallback, BluetoothDevice.TRANSPORT_LE
            )
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
        }
    }

    private fun detectSide(name: String): Side = when {
        name.contains("(L)") || name.contains("Left") -> Side.LEFT
        name.contains("(R)") || name.contains("Right") -> Side.RIGHT
        name.contains("Pro Controller2") || name.contains("Pro") -> Side.PRO
        else -> Side.UNKNOWN
    }

    // ---- GATT callback ------------------------------------------------------

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected. Requesting MTU $DESIRED_MTU")
                onState(Joycon2State(connected = true, connecting = false, side = side))
                // Request a larger MTU BEFORE discovering services so the
                // 63-byte notifications aren't truncated to the 23-byte default.
                g.requestMtu(DESIRED_MTU)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.w(TAG, "Disconnected (status=$status)")
                onState(Joycon2State(connected = false, connecting = false, side = side))
                opQueue.clear()
                opInFlight = false
            }
        }

        @SuppressLint("MissingPermission")
        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            Log.i(TAG, "MTU now $mtu (status=$status). Discovering services.")
            g.discoverServices()
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Service discovery failed: $status")
                return
            }
            val svc = g.getService(INPUT_SERVICE)
            if (svc == null) {
                Log.e(TAG, "Input service not found")
                return
            }
            writeChar = svc.getCharacteristic(WRITE_CHAR)
            notifyChar = svc.getCharacteristic(NOTIFY_CHAR)
            if (writeChar == null || notifyChar == null) {
                Log.e(TAG, "Required characteristics missing (write=$writeChar notify=$notifyChar)")
                return
            }
            writeChar!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

            // Sequence: enable notifications (setNotify + CCCD write), then the
            // two init writes. Each goes through the op queue.
            g.setCharacteristicNotification(notifyChar, true)
            enqueue {
                val cccd = notifyChar!!.getDescriptor(CCCD)
                cccd.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                g.writeDescriptor(cccd)
            }
            enqueueInitWrite(g, INIT_CMD_1)
            enqueueInitWrite(g, INIT_CMD_2)
        }

        override fun onDescriptorWrite(
            g: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            Log.i(TAG, "CCCD write status=$status")
            opComplete()
        }

        override fun onCharacteristicWrite(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic, status: Int
        ) {
            // Pace the next op. We also add a small delay between the two init
            // writes to mirror the 500 ms gap the working macOS code uses.
            mainHandler.postDelayed({ opComplete() }, INIT_GAP_MS)
        }

        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            g: BluetoothGatt, ch: BluetoothGattCharacteristic
        ) {
            if (ch.uuid != NOTIFY_CHAR) return
            val data = ch.value ?: return
            val state = parsePacket(data, side) ?: return
            onState(state)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enqueueInitWrite(g: BluetoothGatt, bytes: ByteArray) {
        enqueue {
            writeChar!!.value = bytes
            writeChar!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            @Suppress("DEPRECATION")
            g.writeCharacteristic(writeChar)
        }
    }

    // ---- Parsing (offsets from working macOS code) --------------------------

    private fun parsePacket(d: ByteArray, side: Side): Joycon2State? {
        if (d.size < 0x3E) return null // need through 0x3D for triggers
        val bb = ByteBuffer.wrap(d).order(ByteOrder.LITTLE_ENDIAN)

        fun u16(off: Int) = (bb.getShort(off).toInt() and 0xFFFF)
        fun i16(off: Int) = bb.getShort(off).toInt()
        fun u8(off: Int) = (d[off].toInt() and 0xFF)

        val buttons = bb.getInt(0x03).toLong() and 0xFFFFFFFFL

        // 12-bit packed sticks
        fun stick(off: Int): Pair<Int, Int> {
            val v = (d[off].toInt() and 0xFF) or
                ((d[off + 1].toInt() and 0xFF) shl 8) or
                ((d[off + 2].toInt() and 0xFF) shl 16)
            val x = v and 0xFFF
            val y = (v shr 12) and 0xFFF
            return x to y
        }

        val (lx, ly) = stick(0x0A)
        val (rx, ry) = stick(0x0D)

        return Joycon2State(
            connected = true,
            connecting = false,
            side = side,
            packetId = (d[0].toInt() and 0xFF) or
                ((d[1].toInt() and 0xFF) shl 8) or
                ((d[2].toInt() and 0xFF) shl 16),
            buttons = buttons,
            pressed = decodeButtons(buttons),
            leftStickX = lx, leftStickY = ly,
            rightStickX = rx, rightStickY = ry,
            triggerL = u8(0x3C), triggerR = u8(0x3D),
            accelX = i16(0x30), accelY = i16(0x32), accelZ = i16(0x34),
            gyroX = i16(0x36), gyroY = i16(0x38), gyroZ = i16(0x3A),
            batteryVolts = u16(0x1F) / 1000f,
        )
    }

    enum class Side { LEFT, RIGHT, PRO, UNKNOWN }

    // Button bitmask map from the working code (full union of L + R + Pro).
    private val buttonMasks: List<Pair<Long, String>> = listOf(
        0x80000000L to "ZL", 0x40000000L to "L", 0x00010000L to "-",
        0x00080000L to "LS", 0x01000000L to "Down", 0x02000000L to "Up",
        0x04000000L to "Right", 0x08000000L to "Left", 0x00200000L to "Camera",
        0x10000000L to "SR(L)", 0x20000000L to "SL(L)", 0x00100000L to "Home",
        0x00400000L to "Chat", 0x00020000L to "+", 0x00001000L to "SR(R)",
        0x00002000L to "SL(R)", 0x00004000L to "R", 0x00008000L to "ZR",
        0x00040000L to "RS", 0x00000100L to "Y", 0x00000200L to "X",
        0x00000400L to "B", 0x00000800L to "A",
    )

    private fun decodeButtons(buttons: Long): Set<String> =
        buttonMasks.filter { (buttons and it.first) != 0L }.map { it.second }.toSet()
}

/** Immutable snapshot of controller state pushed to the UI on every packet. */
data class Joycon2State(
    val connected: Boolean = false,
    val connecting: Boolean = false,
    val side: Joycon2Manager.Side = Joycon2Manager.Side.UNKNOWN,
    val packetId: Int = 0,
    val buttons: Long = 0,
    val pressed: Set<String> = emptySet(),
    val leftStickX: Int = 2048, val leftStickY: Int = 2048,
    val rightStickX: Int = 2048, val rightStickY: Int = 2048,
    val triggerL: Int = 0, val triggerR: Int = 0,
    val accelX: Int = 0, val accelY: Int = 0, val accelZ: Int = 0,
    val gyroX: Int = 0, val gyroY: Int = 0, val gyroZ: Int = 0,
    val batteryVolts: Float = 0f,
)
