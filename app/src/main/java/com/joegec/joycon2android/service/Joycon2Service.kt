package com.joegec.joycon2android.service

import android.app.NotificationManager
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Keeps BLE alive past the Activity. Holds no state: that lives in [AppContainer]. */
class Joycon2Service : Service() {

    inner class LocalBinder : Binder() {
        val service: Joycon2Service get() = this@Joycon2Service
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val container: AppContainer get() = (application as JoyconApplication).container
    private lateinit var wakeLock: PartialWakeLock
    private val notification by lazy { Joycon2Notification(this).apply { createChannel() } }

    @Volatile
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        wakeLock = PartialWakeLock(this, WAKE_LOCK_TAG)
        wakeLock.acquire()
        // Foreground only while connected or scanning; otherwise the bound Activity keeps us alive.
        serviceScope.launch {
            container.observeSession().map { it.foregroundReason() }.distinctUntilChanged().collect(::updateForeground)
        }
    }

    override fun onDestroy() {
        container.stopOutputs()
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
            ACTION_SCAN -> scan()
        }
        return START_STICKY
    }

    // A widget tap starts us with startForegroundService, which must reach startForeground even if the scan can't start.
    private fun scan() {
        enterForeground()
        if (container.scanReadiness.blocker() == null) container.startScan()
        updateForeground(container.observeSession().value.foregroundReason())
        container.scanReadiness.recheck()
    }

    private fun updateForeground(reason: ForegroundReason?) {
        when {
            reason != null && !isForeground -> promote()
            reason != null -> showNotification(reason)
            isForeground && !container.controllerRepository.scanning.value -> demote()
        }
    }

    // startForeground must be reached via a started service; promote ourselves
    private fun promote() {
        try {
            ContextCompat.startForegroundService(
                this,
                Intent(this, Joycon2Service::class.java).setAction(ACTION_GO_FOREGROUND),
            )
        } catch (e: Exception) {
            Log.w(TAG, "Could not enter foreground: ${e.message}")
        }
    }

    private fun demote() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        isForeground = false
        stopSelf() // drop the started state; the bound Activity (if any) keeps us alive
    }

    private fun enterForeground() {
        if (isForeground) return
        val notification = notification.build(currentReason())
        try {
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
        } catch (e: Exception) {
            Log.w(TAG, "Could not enter foreground: ${e.message}")
        }
    }

    private fun showNotification(reason: ForegroundReason) {
        getSystemService(NotificationManager::class.java).notify(Joycon2Notification.ID, notification.build(reason))
    }

    // A widget scan enters the foreground before its scan starts.
    private fun currentReason(): ForegroundReason =
        container.observeSession().value.foregroundReason() ?: ForegroundReason.SCANNING

    companion object {
        const val ACTION_DISCONNECT_ALL = "com.joegec.joycon2android.DISCONNECT_ALL"
        const val ACTION_GO_FOREGROUND = "com.joegec.joycon2android.GO_FOREGROUND"
        const val ACTION_SCAN = "com.joegec.joycon2android.SCAN"
        private const val TAG = "Joycon2Service"
        private const val WAKE_LOCK_TAG = "Joycon2Android::Joycon2Service"
    }
}
