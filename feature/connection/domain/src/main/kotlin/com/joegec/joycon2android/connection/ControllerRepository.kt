package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Side
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

    /** Null returns the controller to the type its advertisement named. */
    fun setControllerType(address: String, side: Side?)
    fun emitError(message: String)
}
