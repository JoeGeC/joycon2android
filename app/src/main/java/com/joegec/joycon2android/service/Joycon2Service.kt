package com.joegec.joycon2android.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.joegec.joycon2android.MainActivity
import com.joegec.joycon2android.R
import com.joegec.joycon2android.ble.Joycon2Manager
import com.joegec.joycon2android.ble.JoyconConnection
import com.joegec.joycon2android.domain.PlayerAssignmentManager
import com.joegec.joycon2android.model.AppUiState
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.model.Side
import com.joegec.joycon2android.uhid.GamepadManager
import com.joegec.joycon2android.uhid.ShizukuPermissionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Foreground service that owns all BLE connections, player assignments, and virtual gamepads.
 * Survives independently of the Activity/ViewModel lifecycle.
 */
class Joycon2Service : Service() {

    inner class LocalBinder : Binder() {
        val service: Joycon2Service get() = this@Joycon2Service
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var manager: Joycon2Manager
    private val assignmentManager = PlayerAssignmentManager()
    private lateinit var gamepadManager: GamepadManager

    private val connectionJobs = mutableMapOf<String, Job>()

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _gamepadEnabled = MutableStateFlow(false)
    val gamepadEnabled: StateFlow<Boolean> = _gamepadEnabled.asStateFlow()

    private val _gamepadError = MutableStateFlow<String?>(null)
    val gamepadError: StateFlow<String?> = _gamepadError.asStateFlow()

    private val playerStateFlows = PlayerNumber.entries.associateWith {
        MutableStateFlow(PlayerState(it))
    }

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        manager = Joycon2Manager(this)
        gamepadManager = GamepadManager(serviceScope, this)

        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        acquireWakeLock()

        serviceScope.launch {
            combine(
                manager.connections,
                manager.scanning,
                manager.error,
                assignmentManager.assignments,
            ) { connections, scanning, error, assignments ->
                manageFlowCollectors(connections)
                buildUiState(connections, scanning, error, assignments)
            }.collect { state ->
                _uiState.value = state
                if (_gamepadEnabled.value) {
                    for (playerState in state.players) {
                        playerStateFlows[playerState.player]?.value = playerState
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        gamepadManager.destroyAll()
        manager.disconnectAll()
        cancelAllCollectors()
        releaseWakeLock()
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

    fun disconnectAll() {
        disableGamepad()
        assignmentManager.unassignAll()
        cancelAllCollectors()
        manager.disconnectAll()
    }

    fun assignToPlayer(address: String, player: PlayerNumber) {
        val connection = manager.getConnection(address) ?: return
        if (!assignmentManager.assign(address, connection.side, player)) return
        connection.setPlayerLed(player)

        if (_gamepadEnabled.value) {
            serviceScope.launch {
                val flow = playerStateFlows[player]!!
                if (gamepadManager.createGamepad(player)) {
                    gamepadManager.startReporting(player, flow)
                }
            }
        }
    }

    fun unassign(address: String) {
        val player = assignmentManager.assignments.value[address]
        assignmentManager.unassign(address)
        manager.getConnection(address)?.clearPlayerLed()

        if (_gamepadEnabled.value && player != null) {
            gamepadManager.destroyGamepad(player)
            if (gamepadManager.activeCount == 0) {
                disableGamepad()
            }
        }
    }

    fun disconnect(address: String) {
        assignmentManager.unassign(address)
        manager.disconnect(address)
    }

    fun emitError(message: String) {
        manager.emitError(message)
    }

    fun enableGamepad() {
        if (_gamepadEnabled.value) return

        if (ShizukuPermissionHandler.isShizukuAvailable) {
            ShizukuPermissionHandler.requestPermission { granted ->
                if (granted) {
                    serviceScope.launch { startGamepadOutput() }
                } else {
                    _gamepadError.value = "Shizuku permission denied"
                }
            }
        } else {
            _gamepadError.value = "Shizuku is not running"
        }
    }

    fun disableGamepad() {
        _gamepadEnabled.value = false
        gamepadManager.destroyAll()
    }

    // --- Private ---

    private suspend fun startGamepadOutput() {
        val activePlayers = _uiState.value.activePlayers

        if (activePlayers.isEmpty()) {
            _gamepadError.value = "No controllers assigned"
            return
        }

        var anyCreated = false
        for (playerState in activePlayers) {
            if (gamepadManager.createGamepad(playerState.player)) {
                val flow = playerStateFlows[playerState.player]!!
                flow.value = playerState
                gamepadManager.startReporting(playerState.player, flow)
                anyCreated = true
            }
        }

        if (anyCreated) {
            _gamepadEnabled.value = true
            _gamepadError.value = null
        } else {
            _gamepadError.value = "Failed to create virtual gamepad — check Shizuku/root"
        }
    }

    private fun manageFlowCollectors(connections: Map<String, JoyconConnection>) {
        val currentAddresses = connections.keys

        val removed = connectionJobs.keys - currentAddresses
        removed.forEach { address ->
            connectionJobs.remove(address)?.cancel()
            assignmentManager.unassign(address)
        }

        val added = currentAddresses - connectionJobs.keys
        added.forEach { address ->
            val connection = connections[address] ?: return@forEach
            connectionJobs[address] = serviceScope.launch {
                launch { connection.connectionState.collectLatest { rebuildState() } }
                launch { connection.input.collectLatest { rebuildState() } }
            }
        }
    }

    private fun rebuildState() {
        val state = buildUiState(
            manager.connections.value,
            manager.scanning.value,
            manager.error.value,
            assignmentManager.assignments.value,
        )
        _uiState.value = state

        if (_gamepadEnabled.value) {
            for (playerState in state.players) {
                playerStateFlows[playerState.player]?.value = playerState
            }
        }
    }

    private fun buildUiState(
        connections: Map<String, JoyconConnection>,
        scanning: Boolean,
        error: String?,
        assignments: Map<String, PlayerNumber>,
    ): AppUiState {
        val joycons = connections.map { (address, connection) ->
            ConnectedJoycon(
                address = address,
                side = connection.side,
                deviceName = connection.deviceName,
                connectionState = connection.connectionState.value,
                input = connection.input.value,
                assignedPlayer = assignments[address],
                ready = connection.initComplete,
            )
        }

        val unassigned = joycons.filter { it.assignedPlayer == null }
        val players = PlayerNumber.entries.map { player ->
            val assigned = joycons.filter { it.assignedPlayer == player }
            PlayerState(
                player = player,
                left = assigned.find { it.side == Side.LEFT },
                right = assigned.find { it.side == Side.RIGHT || it.side == Side.PRO || it.side == Side.UNKNOWN },
            )
        }

        return AppUiState(
            scanning = scanning,
            error = error,
            unassignedJoycons = unassigned,
            players = players,
        )
    }

    private fun cancelAllCollectors() {
        connectionJobs.values.forEach { it.cancel() }
        connectionJobs.clear()
    }

    @SuppressLint("NewApi")
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Joy-Con Service",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Joy-Con BLE connections active"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPending = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = Intent(this, Joycon2Service::class.java).apply {
            action = ACTION_DISCONNECT_ALL
        }
        val disconnectPending = PendingIntent.getService(
            this, 0, disconnectIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Joy-Con Connected")
            .setContentText("BLE connections are active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(openPending)
            .addAction(0, "Disconnect All", disconnectPending)
            .build()
    }

    @SuppressLint("WakelockTimeout")
    private fun acquireWakeLock() {
        val pm = getSystemService(PowerManager::class.java)
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Joycon2Android::Joycon2Service",
        ).apply { acquire() }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { if (it.isHeld) it.release() }
        wakeLock = null
    }

    companion object {
        const val CHANNEL_ID = "joycon2_service"
        const val NOTIFICATION_ID = 1
        const val ACTION_DISCONNECT_ALL = "com.joegec.joycon2android.DISCONNECT_ALL"
    }
}
