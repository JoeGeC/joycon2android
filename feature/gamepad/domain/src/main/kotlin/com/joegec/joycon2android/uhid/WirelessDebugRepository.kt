package com.joegec.joycon2android.uhid

import kotlinx.coroutines.flow.StateFlow

/**
 * The privileged-access path for the gamepad: Shizuku when present, otherwise the in-app
 * ADB / wireless-debugging connection. Surfaces pairing/connection state; the framework
 * layer shows the pairing-code notification in response to [pairingServiceAvailable].
 */
interface WirelessDebugRepository {
    val adbState: StateFlow<AdbState>
    val adbError: StateFlow<String?>
    val pairingServiceAvailable: StateFlow<Boolean>
    val shizukuAvailable: Boolean

    var onConnected: (() -> Unit)?
    var onConnectionLost: (() -> Unit)?

    fun startDiscovery()
    fun stopDiscovery()
    fun startPairing()
    fun submitPairingCode(code: String)
}
