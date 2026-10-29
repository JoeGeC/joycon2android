package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerNumber
import com.joegec.joycon2android.model.Rumble
import com.joegec.joycon2android.model.ControllerModel
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
    fun vibrate(address: String, sample: VibrationSample)

    /** Held until the next call; [Rumble.OFF] stops it. */
    fun rumble(address: String, rumble: Rumble)

    /** Null returns the controller to the type its advertisement named. */
    fun setControllerType(address: String, model: ControllerModel?)
    fun emitError(message: String)
}
