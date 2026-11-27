package com.joegec.joycon2android.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart

/** Checked where a scan starts without the app open to ask for what's missing. */
class ScanReadiness(private val context: Context) {

    private val permissions = BlePermissionHandler(context)
    private val location = LocationServices(context)
    private val bluetooth = context.getSystemService(BluetoothManager::class.java)?.adapter
    private val rechecks = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun blocker(): ScanBlocker? = when {
        !permissions.isGranted() -> ScanBlocker.PERMISSION
        bluetooth?.isEnabled != true -> ScanBlocker.BLUETOOTH_OFF
        location.blocksScanning() -> ScanBlocker.LOCATION_OFF
        else -> null
    }

    /** Android broadcasts no permission grant, so the app calls this once it may have asked. */
    fun recheck() {
        rechecks.tryEmit(Unit)
    }

    fun observe(): Flow<ScanBlocker?> =
        merge(rechecks, bluetoothChanges(), location.observeBlocksScanning().map { })
            .onStart { emit(Unit) }
            .map { blocker() }
            .distinctUntilChanged()

    private fun bluetoothChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(Unit)
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
