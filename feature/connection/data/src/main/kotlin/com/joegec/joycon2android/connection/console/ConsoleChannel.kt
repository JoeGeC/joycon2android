package com.joegec.joycon2android.connection.console

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import com.joegec.joycon2android.model.Side
import java.util.UUID

/** Characteristics and command framing: docs/protocol.md#console-protocol-controllers */
internal class ConsoleChannel(
    val side: Side,
    val command: BluetoothGattCharacteristic,
    val input: BluetoothGattCharacteristic,
    val extendedResponse: BluetoothGattCharacteristic?,
) {
    companion object {
        const val COMMAND_PREFIX_LENGTH = 17

        val RESPONSE: UUID = UUID.fromString("c765a961-d9d8-4d36-a20a-5315b111836a")
        val SESSION_START: UUID = UUID.fromString("00c5af5d-1964-4e30-8f51-1956f96bd282")
        val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val REPORT_RATE: UUID = UUID.fromString("679d5510-5a24-4dee-9557-95df80486ecb")

        private val LEFT_COMMAND = UUID.fromString("ce49a830-dced-48ae-931e-c8cf88aadbea")
        private val LEFT_INPUT = UUID.fromString("cc1bbbb5-7354-4d32-a716-a81cb241a32a")
        private val LEFT_EXTENDED_RESPONSE = UUID.fromString("63a3810f-aec7-474b-9010-3d52403cb996")
        private val RIGHT_COMMAND = UUID.fromString("65a724b3-f1e7-4a61-8078-a342376b27ff")
        private val RIGHT_INPUT = UUID.fromString("d5a9e01e-2ffc-4cca-b20c-8b67142bf442")
        private val RIGHT_EXTENDED_RESPONSE = UUID.fromString("640ca58e-0e88-410c-a7f3-426faf2b690b")

        fun find(gatt: BluetoothGatt): ConsoleChannel? =
            resolve(gatt, Side.LEFT, LEFT_COMMAND, LEFT_INPUT, LEFT_EXTENDED_RESPONSE)
                ?: resolve(gatt, Side.RIGHT, RIGHT_COMMAND, RIGHT_INPUT, RIGHT_EXTENDED_RESPONSE)

        fun characteristic(gatt: BluetoothGatt, uuid: UUID): BluetoothGattCharacteristic? =
            gatt.services.firstNotNullOfOrNull { it.getCharacteristic(uuid) }

        private fun resolve(
            gatt: BluetoothGatt,
            side: Side,
            command: UUID,
            input: UUID,
            extendedResponse: UUID,
        ): ConsoleChannel? {
            val commandChar = characteristic(gatt, command) ?: return null
            val inputChar = characteristic(gatt, input) ?: return null
            return ConsoleChannel(side, commandChar, inputChar, characteristic(gatt, extendedResponse))
        }
    }
}
