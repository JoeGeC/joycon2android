package com.joegec.joycon2android.connection.console

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build

/** Identifies console-protocol clones and hides their pairing dialog: docs/protocol.md#android-workarounds */
internal class SecurityRequestReceiver(
    private val onSecurityRequested: (String) -> Boolean,
    private val onPairingFailed: (String) -> Unit,
) : BroadcastReceiver() {

    companion object {
        private const val PRIORITY_AHEAD_OF_SETTINGS = 999
    }

    private var registered = false

    @Synchronized
    fun register(context: Context) {
        if (registered) return
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            priority = PRIORITY_AHEAD_OF_SETTINGS
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(this, filter)
        }
        registered = true
    }

    override fun onReceive(context: Context, intent: Intent) {
        val address = deviceOf(intent)?.address ?: return
        when (intent.action) {
            BluetoothDevice.ACTION_PAIRING_REQUEST -> {
                if (onSecurityRequested(address) && isOrderedBroadcast) abortBroadcast()
            }
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val previous = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)
                val current = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                if (previous == BluetoothDevice.BOND_BONDING && current == BluetoothDevice.BOND_NONE) {
                    onPairingFailed(address)
                }
            }
        }
    }

    private fun deviceOf(intent: Intent): BluetoothDevice? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
}
