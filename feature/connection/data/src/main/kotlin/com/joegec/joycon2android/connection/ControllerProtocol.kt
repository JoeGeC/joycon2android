package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Rumble
import java.util.UUID

/** How one controller is driven: the common channel, or the console one. */
internal interface ControllerProtocol {
    fun start()
    fun stop()
    fun onCharacteristicChanged(uuid: UUID, value: ByteArray)
    fun setPlayerLed(bitmask: Byte)
    fun vibrate(sample: VibrationSample)
    fun rumble(rumble: Rumble)
    fun setHighPriority(enabled: Boolean)
    fun onPairingFailed() {}
}
