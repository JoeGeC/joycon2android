package com.joegec.joycon2android.connection.console

/** Command frames, in the order a console sends them: docs/protocol.md#console-protocol-controllers */
internal object ConsoleCommands {

    const val HEADER_LENGTH = 8

    val HELLO = command(0x07, 0x01)
    val FIRMWARE_INFO = command(0x10, 0x01)
    val UNKNOWN_16_01 = command(0x16, 0x01)
    val UNKNOWN_11_03 = command(0x11, 0x03)
    val UNKNOWN_11_01 = command(0x11, 0x01)
    val PAIRING_FINALISE = command(0x15, 0x03, bytes(0x00))
    val PAIRING_STORE = command(0x03, 0x09)

    val VIBRATION_SAMPLE = command(0x0A, 0x02, bytes(0x03, 0x00, 0x00, 0x00))
    val VIBRATION_DATA = command(
        0x0A, 0x08,
        bytes(
            0x01, 0x59, 0x09, 0x00, 0x00, 0xFF, 0xFF, 0xFF, 0xFF, 0x35,
            0x00, 0x46, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        ),
    )

    // Feature mask 0x37, as the console sends it; includes motion (bit 2) and mouse (bit 4).
    val FEATURES_INIT = command(0x0C, 0x02, bytes(0x37, 0x00, 0x00, 0x00))
    val FEATURES_ENABLE = command(0x0C, 0x04, bytes(0x37, 0x00, 0x00, 0x00))

    const val DEVICE_INFO_ADDRESS = 0x013000
    val SPI_READS_AFTER_FEATURES_INIT = listOf(
        0x40 to 0x013080,
        0x40 to 0x1FC040,
        0x10 to 0x013040,
        0x18 to 0x013100,
    )
    const val SPI_READ_BEFORE_VIBRATION = 0x013060
    const val SPI_READ_BEFORE_VIBRATION_LENGTH = 0x20

    const val LED_ALL_ON: Byte = 0x0F

    // Reply header echoes: id at [0], 0x01 at [1], sub at [3].
    const val REPLY_MARKER: Byte = 0x01

    fun spiRead(length: Int, address: Int) = command(
        0x02, 0x04,
        byteArrayOf(length.toByte(), 0x7E, 0x00, 0x00) + littleEndian(address),
    )

    fun playerLed(bitmask: Byte) = command(0x09, 0x07, byteArrayOf(bitmask) + ByteArray(7))

    fun pairingAddresses(hostAddress: ByteArray, secondAddress: ByteArray) =
        command(0x15, 0x01, bytes(0x00, 0x02) + hostAddress + secondAddress)

    fun pairingExchangeKey(a1: ByteArray) = command(0x15, 0x04, bytes(0x00) + a1)

    fun pairingConfirm(a2: ByteArray) = command(0x15, 0x02, bytes(0x00) + a2)

    fun pairingInfo(secondAddress: ByteArray, reversedLtk: ByteArray) =
        command(0x03, 0x07, secondAddress + reversedLtk)

    fun command(id: Int, sub: Int, data: ByteArray = ByteArray(0)): ByteArray =
        bytes(id, 0x91, 0x01, sub, 0x00, data.size, 0x00, 0x00) + data

    private fun littleEndian(value: Int) = bytes(value, value shr 8, value shr 16, value shr 24)

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
}
