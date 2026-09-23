package com.joegec.joycon2android.connection.console

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.os.Build
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass

/** 7.5 ms, the LE minimum: docs/protocol.md#console-protocol-controllers */
@SuppressLint("MissingPermission")
internal object ConnectionInterval {

    private const val TAG = "Joycon2"

    // Units of 1.25 ms and 10 ms: 6 * 1.25 = 7.5 ms, no peripheral latency, 5 s supervision timeout.
    private val FASTEST = arrayOf<Any>(6, 6, 0, 500, 0, 0)

    fun requestFastest(gatt: BluetoothGatt): Boolean {
        val accepted = runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                HiddenApiBypass.invoke(BluetoothGatt::class.java, gatt, "requestLeConnectionUpdate", *FASTEST)
            } else {
                BluetoothGatt::class.java
                    .getMethod("requestLeConnectionUpdate", *Array(FASTEST.size) { Int::class.java })
                    .invoke(gatt, *FASTEST)
            } == true
        }.onFailure { Log.w(TAG, "7.5 ms interval request unavailable: $it") }.getOrDefault(false)

        if (!accepted) gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
        return accepted
    }
}
