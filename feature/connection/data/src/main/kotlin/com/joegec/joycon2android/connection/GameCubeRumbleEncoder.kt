package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.Rumble

/** An on/off motor, on at any strength: docs/protocol.md#gamecube-rumble */
internal class GameCubeRumbleEncoder : RumbleEncoder {

    private var counter = 0

    override fun packet(rumble: Rumble): ByteArray = packet(if (rumble.isOn) ON else BRAKE)

    override fun packet(amplitude: Int): ByteArray = packet(if (amplitude > 0) ON else BRAKE)

    private fun packet(state: Byte) =
        byteArrayOf(REPORT_ID, (HEADER or (counter++ and 0x0F)).toByte(), state, 0x00, 0x00)

    private companion object {
        const val REPORT_ID: Byte = 0x00
        const val HEADER = 0x50
        const val ON: Byte = 0x01
        const val BRAKE: Byte = 0x02
    }
}
