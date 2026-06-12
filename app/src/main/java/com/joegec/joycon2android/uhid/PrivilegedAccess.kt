package com.joegec.joycon2android.uhid

import android.content.Context
import android.util.Log
import com.joegec.joycon2android.adb.AdbPairingNotification
import com.joegec.joycon2android.adb.AdbShell
import io.github.muntashirakon.adb.android.AdbMdns
import java.net.InetAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AdbState { DISCONNECTED, WORKING, CONNECTED }

/**
 * Chooses how the virtual gamepad reaches `/dev/uhid`: Shizuku when it's installed and
 * running (zero extra setup for users who have it), otherwise an in-app ADB connection
 * to the device's own wireless-debugging daemon.
 *
 * The ADB path is otherwise automatic — mDNS supplies the pairing and connection
 * endpoints, so the user only types the pairing code (into a notification, since the
 * system pairing dialog closes the moment the app is foregrounded), and reconnection
 * after a reboot happens on its own once wireless debugging is back on.
 */
class PrivilegedAccess(private val context: Context, private val scope: CoroutineScope) {

    private val shizuku = ShizukuShell()
    private val adb = AdbShell(context)
    private val notification = AdbPairingNotification(context)

    private var pairingMdns: AdbMdns? = null
    private var connectMdns: AdbMdns? = null
    private var connectionMonitor: Job? = null

    /** Fired when an established ADB connection drops (e.g. the user revokes pairing). */
    var onConnectionLost: (() -> Unit)? = null

    @Volatile
    private var pairingHost: InetAddress? = null

    @Volatile
    private var pairingPort: Int = -1

    @Volatile
    private var connectHost: InetAddress? = null

    @Volatile
    private var connectPort: Int = -1

    private val _adbState = MutableStateFlow(AdbState.DISCONNECTED)
    val adbState: StateFlow<AdbState> = _adbState.asStateFlow()

    private val _adbError = MutableStateFlow<String?>(null)
    val adbError: StateFlow<String?> = _adbError.asStateFlow()

    val shizukuAvailable: Boolean get() = ShizukuPermissionHandler.isShizukuAvailable

    fun acquire(onResult: (PrivilegedShell?) -> Unit) {
        when {
            shizuku.isReady -> onResult(shizuku)
            ShizukuPermissionHandler.isShizukuAvailable ->
                ShizukuPermissionHandler.requestPermission { granted -> onResult(if (granted) shizuku else null) }
            adb.isReady -> onResult(adb)
            else -> onResult(null)
        }
    }

    /** Auto-reconnect discovery — silent, no notification, safe to run while idle. */
    fun startAdbDiscovery() {
        if (ShizukuPermissionHandler.isShizukuAvailable || connectMdns != null) return
        connectMdns = AdbMdns(context, AdbMdns.SERVICE_TYPE_TLS_CONNECT) { host, port ->
            connectHost = host
            connectPort = port
            if (host != null && port > 0) tryConnect()
        }.apply { start() }
    }

    /** Pairing discovery posts the code notification, so only run it when the user asks. */
    fun startPairing() {
        if (ShizukuPermissionHandler.isShizukuAvailable || pairingMdns != null) return
        notification.createChannel()
        pairingMdns = AdbMdns(context, AdbMdns.SERVICE_TYPE_TLS_PAIRING) { host, port ->
            pairingHost = host
            pairingPort = port
            if (host != null && port > 0) notification.show() else notification.cancel()
        }.apply { start() }
    }

    fun stopPairing() {
        pairingMdns?.stop()
        pairingMdns = null
        notification.cancel()
    }

    fun stopAdbDiscovery() {
        connectMdns?.stop()
        connectMdns = null
        connectionMonitor?.cancel()
        connectionMonitor = null
        stopPairing()
    }

    fun submitPairingCode(code: String) {
        notification.cancel()
        val host = pairingHost
        val port = pairingPort
        if (host == null || port <= 0) {
            _adbError.value = "Pairing window closed — re-open \"Pair device with pairing code\""
            return
        }
        launchAdb(surfaceErrors = true) {
            if (!adb.pair(host.hostAddress ?: return@launchAdb false, port, code)) {
                _adbError.value = "Pairing failed — re-check the code"
                false
            } else {
                connectNow()
            }
        }
    }

    private fun tryConnect() {
        if (_adbState.value != AdbState.DISCONNECTED) return
        launchAdb(surfaceErrors = false) { connectNow() }
    }

    private fun connectNow(): Boolean {
        val host = connectHost?.hostAddress ?: return false
        if (connectPort <= 0) return false
        return adb.connect(host, connectPort)
    }

    private fun launchAdb(surfaceErrors: Boolean, block: suspend () -> Boolean) {
        if (_adbState.value == AdbState.WORKING) return
        _adbState.value = AdbState.WORKING
        if (surfaceErrors) _adbError.value = null
        scope.launch {
            val ok = try {
                withContext(Dispatchers.IO) { block() }
            } catch (e: Exception) {
                // Connect attempts fire on every mDNS tick; only the user's pairing
                // submission should surface an error, not a not-yet-paired auto-attempt
                if (surfaceErrors) _adbError.value = e.message ?: "ADB connection failed"
                else Log.d(TAG, "ADB auto-connect skipped: ${e.message}")
                false
            }
            val connected = ok && adb.isReady
            _adbState.value = if (connected) AdbState.CONNECTED else AdbState.DISCONNECTED
            // Pairing is done once connected; connect-discovery stays on for auto-reconnect
            if (connected) {
                stopPairing()
                startConnectionMonitor()
            }
        }
    }

    // Catches the daemon revoking us while idle — there's no I/O to fail, so poll the link
    private fun startConnectionMonitor() {
        connectionMonitor?.cancel()
        connectionMonitor = scope.launch {
            while (isActive) {
                delay(CONNECTION_POLL_MS)
                if (!adb.isReady) {
                    _adbState.value = AdbState.DISCONNECTED
                    onConnectionLost?.invoke()
                    break
                }
            }
        }
    }

    companion object {
        private const val TAG = "PrivilegedAccess"
        private const val CONNECTION_POLL_MS = 2_000L
    }
}
