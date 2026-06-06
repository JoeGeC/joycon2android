package com.joegec.joycon2android.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.content.Context
import com.joegec.joycon2android.model.Side
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages all active Joy-Con BLE connections, keyed by device address.
 * Thread-safe: BLE callbacks arrive on binder threads.
 */
class ConnectionPool(private val context: Context) {

    private val connections = ConcurrentHashMap<String, JoyconConnection>()

    val all: Map<String, JoyconConnection> get() = connections.toMap()
    val addresses: Set<String> get() = connections.keys.toSet()
    val size: Int get() = connections.size

    @SuppressLint("MissingPermission")
    fun connect(result: ScanResult, side: Side, name: String): JoyconConnection {
        val address = result.device.address
        val connection = JoyconConnection(context, side, name)
        connections[address] = connection
        connection.connect(result.device)
        return connection
    }

    fun get(address: String): JoyconConnection? = connections[address]

    @SuppressLint("MissingPermission")
    fun disconnect(address: String) {
        connections.remove(address)?.disconnect()
    }

    @SuppressLint("MissingPermission")
    fun disconnectAll() {
        connections.values.forEach { it.disconnect() }
        connections.clear()
    }
}
