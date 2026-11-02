package com.joegec.joycon2android.ble

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf

class LocationServices(private val context: Context) {

    private val locationManager = context.getSystemService(LocationManager::class.java)

    // Before Android 12, a BLE scan returns no results, and no error, while location is off.
    fun observeBlocksScanning(): Flow<Boolean> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flowOf(false) else observeOff()

    fun buildSettingsIntent(): Intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

    private fun observeOff(): Flow<Boolean> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(isOff())
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION),
            ContextCompat.RECEIVER_EXPORTED,
        )
        send(isOff())
        awaitClose { context.unregisterReceiver(receiver) }
    }.distinctUntilChanged()

    private fun isOff(): Boolean = !LocationManagerCompat.isLocationEnabled(locationManager)
}
