package com.joegec.joycon2android.uhid

import android.content.Context
import android.util.Log
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GamepadManager(private val scope: CoroutineScope, private val context: Context) {

    private val devices = mutableMapOf<PlayerNumber, UhidRelay>()
    private val reportJobs = mutableMapOf<PlayerNumber, Job>()

    val activeCount: Int get() = devices.size

    suspend fun createGamepad(player: PlayerNumber, shell: PrivilegedShell): Boolean = withContext(Dispatchers.IO) {
        if (player in devices) return@withContext true

        val device = UhidRelay("Joy-Con Virtual Gamepad", player.index)
        val success = device.create(context, shell)

        if (success) {
            devices[player] = device
            Log.i(TAG, "Created virtual gamepad for ${player.name}")
        } else {
            Log.e(TAG, "Failed to create virtual gamepad for ${player.name}")
        }
        success
    }

    fun startReporting(player: PlayerNumber, stateFlow: StateFlow<PlayerState>) {
        reportJobs[player]?.cancel()
        val device = devices[player] ?: return
        reportJobs[player] = scope.launch(Dispatchers.Default) {
            stateFlow.collect { state ->
                val report = ReportMapper.buildReport(state)
                device.sendReport(report)
            }
        }
    }

    fun destroyGamepad(player: PlayerNumber) {
        reportJobs.remove(player)?.cancel()
        // Teardown writes a shutdown packet; over ADB that's a TLS socket, so off-main
        val device = devices.remove(player) ?: return
        scope.launch(Dispatchers.IO) { device.destroy() }
        Log.i(TAG, "Destroyed virtual gamepad for ${player.name}")
    }

    fun destroyAll() {
        reportJobs.values.forEach { it.cancel() }
        reportJobs.clear()
        val toDestroy = devices.values.toList()
        devices.clear()
        if (toDestroy.isNotEmpty()) scope.launch(Dispatchers.IO) { toDestroy.forEach { it.destroy() } }
        Log.i(TAG, "Destroyed all virtual gamepads")
    }

    companion object {
        private const val TAG = "GamepadManager"
    }
}
