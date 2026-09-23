package com.joegec.joycon2android.connection

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.content.Context
import com.joegec.joycon2android.connection.console.SecurityRequestReceiver
import com.joegec.joycon2android.model.Side
import java.util.concurrent.ConcurrentHashMap

/** Thread-safe: BLE callbacks arrive on binder threads. */
@SuppressLint("MissingPermission")
class ConnectionPool(
    private val context: Context,
    private val hostAddress: () -> String? = { null },
) {

    private val connections = ConcurrentHashMap<String, JoyconConnection>()
    private val securityRequests = SecurityRequestReceiver(
        onSecurityRequested = { address -> connections[address]?.let { it.onSecurityRequested(); true } ?: false },
        onPairingFailed = { address -> connections[address]?.onPairingFailed() },
    )

    var onPoolChanged: (() -> Unit)? = null

    val all: Map<String, JoyconConnection> get() = connections.toMap()
    val addresses: Set<String> get() = connections.keys.toSet()
    val size: Int get() = connections.size

    /** Null for an address already in the pool (a duplicate scan result). */
    fun connect(result: ScanResult, side: Side, name: String, highPriority: Boolean): JoyconConnection? {
        val address = result.device.address
        val connection = JoyconConnection(
            context,
            side,
            name,
            hostAddress,
            onReady = { reassertOtherPriorities(address) },
        ) {
            connections.remove(address)
            onPoolChanged?.invoke()
        }
        connection.setHighPriority(highPriority)
        if (connections.putIfAbsent(address, connection) != null) return null
        securityRequests.register(context)
        connection.connect(result.device)
        return connection
    }

    fun get(address: String): JoyconConnection? = connections[address]

    fun setHighPriority(enabled: Boolean) {
        connections.values.forEach { it.setHighPriority(enabled) }
    }

    // Android can slow an existing connection when another controller connects.
    private fun reassertOtherPriorities(joined: String) {
        connections.forEach { (address, connection) ->
            if (address != joined) connection.reassertPriority()
        }
    }

    fun disconnect(address: String) {
        connections.remove(address)?.disconnect()
    }

    fun disconnectAll() {
        connections.values.forEach { it.disconnect() }
        connections.clear()
    }
}
