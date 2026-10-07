package com.joegec.joycon2android.connection.console

import com.joegec.joycon2android.connection.ControllerCommands.command
import com.joegec.joycon2android.connection.ControllerCommands.enableFeatures
import com.joegec.joycon2android.connection.ControllerCommands.selectFeatures

/** Command frames, in the order a console sends them: docs/protocol.md#console-protocol-controllers */
internal object ConsoleCommands {

    val HELLO = command(0x07, 0x01)
    val FIRMWARE_INFO = command(0x10, 0x01)
    val UNKNOWN_16_01 = command(0x16, 0x01)
    val UNKNOWN_11_03 = command(0x11, 0x03)
    val UNKNOWN_11_01 = command(0x11, 0x01)

    val VIBRATION_SAMPLE = command(0x0A, 0x02, bytes(0x03, 0x00, 0x00, 0x00))
    val VIBRATION_DATA = command(
        0x0A, 0x08,
        bytes(
            0x01, 0x59, 0x09, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x35,
            0x00, 0x46, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        ),
    )

    // Feature mask 0x37, as the console sends it; includes motion (bit 2) and mouse (bit 4).
    private const val FEATURES: Byte = 0x37
    val FEATURES_INIT = selectFeatures(FEATURES)
    val FEATURES_ENABLE = enableFeatures(FEATURES)

    val SPI_READS_AFTER_FEATURES_INIT = listOf(
        0x40 to 0x013080,
        0x40 to 0x1FC040,
        0x10 to 0x013040,
        0x18 to 0x013100,
    )
    const val SPI_READ_BEFORE_VIBRATION = 0x013060
    const val SPI_READ_BEFORE_VIBRATION_LENGTH = 0x20

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
}
