package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import kotlinx.coroutines.flow.StateFlow

/**
 * Connected Joy-Cons as domain entities. The data layer assembles each [ConnectedJoycon]
 * (live input + connection state) from its BLE connection, so callers never see the raw
 * GATT layer. [controllers] re-emits on every input/state change.
 */
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
