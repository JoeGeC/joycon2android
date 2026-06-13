package com.joegec.joycon2android.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import com.joegec.joycon2android.AppContainer
import com.joegec.joycon2android.JoyconApplication
import com.joegec.joycon2android.adb.AdbPairingNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Keeps the app's BLE connections and outputs alive past the Activity: holds a wake lock
 * and promotes to a foreground service (with notification) while a Joy-Con is connected.
 * All app state lives in [AppContainer]; the Activity binds this only for that lifetime.
 */
class Joycon2Service : Service() {

    inner class LocalBinder : Binder() {
        val service: Joycon2Service get() = this@Joycon2Service
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val container: AppContainer get() = (application as JoyconApplication).container
    private val pairingNotification by lazy { AdbPairingNotification(this) }
    private lateinit var wakeLock: PartialWakeLock

    @Volatile
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        wakeLock = PartialWakeLock(this, WAKE_LOCK_TAG)
        wakeLock.acquire()
        container.startWirelessDiscovery()
        // Foreground (and its notification) only while a Joy-Con is actually connected;
        // otherwise the bound Activity keeps us alive without a notification
        serviceScope.launch {
            container.observeSession().collect { updateForeground(it.anyConnected) }
        }
        // The pairing code is entered from a notification (the system dialog closes when
        // we foreground); show it whenever a pairing service is being advertised
        pairingNotification.createChannel()
        serviceScope.launch {
            container.observeWirelessDebugStatus().collect { status ->
                if (status.pairingServiceAvailable) pairingNotification.show() else pairingNotification.cancel()
            }
        }
    }

    override fun onDestroy() {
        container.disableGamepad()
        container.disableDsu()
        container.stopWirelessDiscovery()
        container.controllerRepository.disconnectAll()
        wakeLock.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT_ALL -> {
                container.disconnectAll()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_GO_FOREGROUND -> enterForeground()
            ACTION_ADB_PAIR_CODE -> intent.getStringExtra(EXTRA_PAIR_CODE)?.let(container.submitPairingCode::invoke)
        }
        return START_STICKY
    }

    private fun updateForeground(connected: Boolean) {
        if (connected && !isForeground) {
            // startForeground must be reached via a started service; promote ourselves
            try {
                ContextCompat.startForegroundService(
                    this,
                    Intent(this, Joycon2Service::class.java).setAction(ACTION_GO_FOREGROUND),
                )
            } catch (e: Exception) {
                Log.w(TAG, "Could not enter foreground: ${e.message}")
            }
        } else if (!connected && isForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isForeground = false
            stopSelf() // drop the started state; the bound Activity (if any) keeps us alive
        }
    }

    private fun enterForeground() {
        if (isForeground) return
        val notification = Joycon2Notification(this).apply { createChannel() }.build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                Joycon2Notification.ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
            )
        } else {
            startForeground(Joycon2Notification.ID, notification)
        }
        isForeground = true
    }

    companion object {
        const val ACTION_DISCONNECT_ALL = "com.joegec.joycon2android.DISCONNECT_ALL"
        const val ACTION_GO_FOREGROUND = "com.joegec.joycon2android.GO_FOREGROUND"
        const val ACTION_ADB_PAIR_CODE = "com.joegec.joycon2android.ADB_PAIR_CODE"
        const val EXTRA_PAIR_CODE = "pair_code"
        private const val TAG = "Joycon2Service"
        private const val WAKE_LOCK_TAG = "Joycon2Android::Joycon2Service"
    }
}
