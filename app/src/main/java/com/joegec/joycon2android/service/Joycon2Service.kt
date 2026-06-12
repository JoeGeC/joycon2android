package com.joegec.joycon2android.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.joegec.joycon2android.ble.Joycon2Manager
import com.joegec.joycon2android.domain.ControllerAssigner
import com.joegec.joycon2android.domain.PlayerAssignmentManager
import com.joegec.joycon2android.domain.PlayerStateResolver
import com.joegec.joycon2android.domain.UiStateAggregator
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.uhid.GamepadManager
import com.joegec.joycon2android.uhid.GamepadOutput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

/**
 * Owns BLE connections, player assignments, and virtual gamepads so they
 * survive independently of the Activity/ViewModel lifecycle.
 */
class Joycon2Service : Service() {

    inner class LocalBinder : Binder() {
        val service: Joycon2Service get() = this@Joycon2Service
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val assignments = PlayerAssignmentManager()

    private lateinit var manager: Joycon2Manager
    private lateinit var gamepads: GamepadOutput
    private lateinit var assigner: ControllerAssigner
    private lateinit var aggregator: UiStateAggregator
    private lateinit var wakeLock: PartialWakeLock

    val uiState: StateFlow<AppUiState> get() = aggregator.uiState
    val gamepadEnabled: StateFlow<Boolean> get() = gamepads.enabled
    val gamepadError: StateFlow<String?> get() = gamepads.error

    override fun onCreate() {
        super.onCreate()
        buildCollaborators()
        startInForeground()
        wakeLock.acquire()
        aggregator.start()
    }

    override fun onDestroy() {
        gamepads.destroyAll()
        manager.disconnectAll()
        aggregator.stopInputCollectors()
        wakeLock.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISCONNECT_ALL) {
            disconnectAll()
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    // --- Public API for ViewModel ---

    fun startScan() = manager.startScan()
    fun stopScan() = manager.stopScan()
    fun assignToPlayer(address: String, player: PlayerNumber) = assigner.assign(address, player)
    fun unassign(address: String) = assigner.unassign(address)
    fun enableGamepad() = gamepads.enable()
    fun disableGamepad() = gamepads.disable()
    fun emitError(message: String) = manager.emitError(message)

    fun disconnect(address: String) {
        assignments.unassign(address)
        manager.disconnect(address)
    }

    fun disconnectAll() {
        gamepads.disable()
        assignments.unassignAll()
        aggregator.stopInputCollectors()
        manager.disconnectAll()
    }

    private fun buildCollaborators() {
        manager = Joycon2Manager(this)
        gamepads = GamepadOutput(serviceScope, GamepadManager(serviceScope, this)) {
            uiState.value.activePlayers
        }
        assigner = ControllerAssigner(
            assignments = assignments,
            connectionFor = manager::getConnection,
            onAssigned = gamepads::onPlayerAssigned,
            onUnassigned = gamepads::onPlayerUnassigned,
        )
        aggregator = UiStateAggregator(
            scope = serviceScope,
            connections = manager.connections,
            scanning = manager.scanning,
            error = manager.error,
            assignments = assignments,
            resolver = PlayerStateResolver(evictConflicting = assignments::unassign),
        ) { state ->
            gamepads.push(state.players)
            assigner.applyCombos(state.unassignedJoycons)
        }
        wakeLock = PartialWakeLock(this, WAKE_LOCK_TAG)
    }

    private fun startInForeground() {
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
    }

    companion object {
        const val ACTION_DISCONNECT_ALL = "com.joegec.joycon2android.DISCONNECT_ALL"
        private const val WAKE_LOCK_TAG = "Joycon2Android::Joycon2Service"
    }
}
