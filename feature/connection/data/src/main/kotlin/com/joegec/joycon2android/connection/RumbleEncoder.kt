package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Rumble

/** Turns rumble into packets for one controller's vibration characteristic: docs/protocol.md#hd-rumble */
internal interface RumbleEncoder {
    fun packet(rumble: Rumble): ByteArray

    /** [amplitude] in HD rumble units, up to [HdRumble.MAX_AMPLITUDE], with no strength cap. */
    fun packet(amplitude: Int): ByteArray
}
