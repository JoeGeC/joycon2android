package com.joegec.joycon2android.connection

/** Command framing, shared by both protocols: docs/protocol.md#commands */
internal object ControllerCommands {

    const val HEADER_LENGTH = 8

    // Transport, at [2] of the header.
    const val BLUETOOTH = 0x01
    const val USB = 0x00

    // A reply echoes the id at [0], this at [1] and the sub-command at [3].
    const val REPLY_MARKER: Byte = 0x01

    const val DEVICE_INFO_ADDRESS = 0x013000
    const val LED_ALL_ON: Byte = 0x0F

    private const val REQUEST = 0x91

    /** Header: id, direction, transport, sub-command, unknown, data length, two reserved bytes. */
    fun command(id: Int, sub: Int, data: ByteArray = ByteArray(0), transport: Int = BLUETOOTH): ByteArray =
        bytes(id, REQUEST, transport, sub, 0x00, data.size, 0x00, 0x00) + data

    fun selectFeatures(mask: Byte) = command(0x0C, 0x02, featureMask(mask))

    fun enableFeatures(mask: Byte) = command(0x0C, 0x04, featureMask(mask))

    fun playerLed(bitmask: Byte) = command(0x09, 0x07, byteArrayOf(bitmask) + ByteArray(7))

    fun vibrationSample(sample: VibrationSample) = command(0x0A, 0x02, byteArrayOf(sample.id, 0x00, 0x00, 0x00))

    private val VibrationSample.id: Byte
        get() = when (this) {
            VibrationSample.CLICK -> 0x03
        }

    fun spiRead(length: Int, address: Int, transport: Int = BLUETOOTH) = command(
        0x02, 0x04,
        byteArrayOf(length.toByte(), 0x7E, 0x00, 0x00) + littleEndian(address),
        transport,
    )

    private fun featureMask(mask: Byte) = byteArrayOf(mask, 0x00, 0x00, 0x00)

    private fun littleEndian(value: Int) = bytes(value, value shr 8, value shr 16, value shr 24)

    private fun bytes(vararg values: Int) = ByteArray(values.size) { values[it].toByte() }
}
