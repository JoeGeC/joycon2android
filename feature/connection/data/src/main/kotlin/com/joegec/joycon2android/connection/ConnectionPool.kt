package com.joegec.joycon2android.connection

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.content.Context
import com.joegec.joycon2android.model.Side
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages all active Joy-Con BLE connections, keyed by device address.
 * Thread-safe: BLE callbacks arrive on binder threads.
 *
 * All BLE operations require BLUETOOTH_CONNECT permission, which is verified
 * by the permission launcher in MainActivity before any BLE code is reached.
 */
@SuppressLint("MissingPermission")
class ConnectionPool(private val context: Context) {

    private val connections = ConcurrentHashMap<String, JoyconConnection>()

    var onPoolChanged: (() -> Unit)? = null

    val all: Map<String, JoyconConnection> get() = connections.toMap()
    val addresses: Set<String> get() = connections.keys.toSet()
    val size: Int get() = connections.size

    /**
     * Atomically creates and starts a connection for [result].
     * Returns null if this address is already in the pool (duplicate scan result).
     */
    fun connect(result: ScanResult, side: Side, name: String): JoyconConnection? {
        val address = result.device.address
        val connection = JoyconConnection(context, side, name) {
            connections.remove(address)
            onPoolChanged?.invoke()
        }
        if (connections.putIfAbsent(address, connection) != null) return null
        connection.connect(result.device)
        return connection
    }

    fun get(address: String): JoyconConnection? = connections[address]

    fun disconnect(address: String) {
        connections.remove(address)?.disconnect()
    }

    fun disconnectAll() {
        connections.values.forEach { it.disconnect() }
        connections.clear()
    }
}
