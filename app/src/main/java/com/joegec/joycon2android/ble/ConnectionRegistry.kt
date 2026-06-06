package com.joegec.joycon2android.ble

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.content.Context
import com.joegec.joycon2android.model.ControllerState
import com.joegec.joycon2android.model.JoyconConnectionState
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side

/**
 * Tracks active Joy-Con connections (up to one per side).
 * Responsible for assigning connections to slots and building combined state.
 */
class ConnectionRegistry(private val context: Context) {

    private var leftConnection: JoyconConnection? = null
    private var rightConnection: JoyconConnection? = null
    private val connectedAddresses = mutableSetOf<String>()

    val left: JoyconConnection? get() = leftConnection
    val right: JoyconConnection? get() = rightConnection
    val knownAddresses: Set<String> get() = connectedAddresses
    val bothConnected: Boolean get() = leftConnection != null && rightConnection != null

    fun hasSide(side: Side): Boolean = when (side) {
        Side.LEFT -> leftConnection != null
        Side.RIGHT -> rightConnection != null
        else -> false
    }

    @SuppressLint("MissingPermission")
    fun connect(result: ScanResult, side: Side, name: String) {
        connectedAddresses.add(result.device.address)
        val connection = JoyconConnection(context, side, name)
        assign(connection, side)
        connection.connect(result.device)
    }

    @SuppressLint("MissingPermission")
    fun disconnectAll() {
        leftConnection?.disconnect()
        rightConnection?.disconnect()
        leftConnection = null
        rightConnection = null
        connectedAddresses.clear()
    }

    fun buildState(scanning: Boolean): ControllerState = ControllerState(
        scanning = scanning,
        left = leftConnection?.connectionState?.value ?: JoyconConnectionState(),
        right = rightConnection?.connectionState?.value ?: JoyconConnectionState(),
        leftInput = leftConnection?.input?.value ?: JoyconInput(),
        rightInput = rightConnection?.input?.value ?: JoyconInput(),
    )

    private fun assign(connection: JoyconConnection, side: Side) {
        when (side) {
            Side.LEFT -> leftConnection = connection
            Side.RIGHT -> rightConnection = connection
            else -> {
                if (leftConnection == null) leftConnection = connection
                else rightConnection = connection
            }
        }
    }
}
