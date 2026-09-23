package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.StateFlow

/** [controllers] re-emits on every input or state change. */
interface ControllerRepository {
    val controllers: StateFlow<List<ConnectedJoycon>>
    val scanning: StateFlow<Boolean>
    val error: StateFlow<String?>

    fun startScan()
    fun stopScan()
    fun disconnect(address: String)
    fun disconnectAll()
    fun setPlayerLed(address: String, player: PlayerNumber?)
    fun emitError(message: String)
}
