package com.joegec.joycon2android.connection

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

/** The channel a Joy-Con 2 speaks to any host: docs/protocol.md#ble-services */
internal class CommonChannel(
    val write: BluetoothGattCharacteristic,
    val input: BluetoothGattCharacteristic,
    val commandResponse: BluetoothGattCharacteristic?,
    /** Null on a GameCube controller, whose rumble takes another format. */
    val vibration: BluetoothGattCharacteristic?,
) {
    val rumbleMotors: Int get() = if (vibration?.uuid == VIBRATION_PRO) 2 else 1

    companion object {
        val INPUT: UUID = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd2")
        val COMMAND_RESPONSE: UUID = UUID.fromString("c765a961-d9d8-4d36-a20a-5315b111836a")
        val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        private val SERVICE = UUID.fromString("ab7de9be-89fe-49ad-828f-118f09df7fd0")
        private val WRITE = UUID.fromString("649d4ac9-8eb7-4e6c-af44-1ea54fe5f005")
        private val VIBRATION_LEFT = UUID.fromString("289326cb-a471-485d-a8f4-240c14f18241")
        private val VIBRATION_RIGHT = UUID.fromString("fa19b0fb-cd1f-46a7-84a1-bbb09e00c149")
        private val VIBRATION_PRO = UUID.fromString("cc483f51-9258-427d-a939-630c31f72b05")

        fun hasService(gatt: BluetoothGatt) = gatt.getService(SERVICE) != null

        /** Null when a controller advertises the service but not the characteristics to drive it. */
        fun find(gatt: BluetoothGatt): CommonChannel? {
            val service = gatt.getService(SERVICE) ?: return null
            val write = service.getCharacteristic(WRITE) ?: return null
            val input = service.getCharacteristic(INPUT) ?: return null
            val vibration = listOf(VIBRATION_LEFT, VIBRATION_RIGHT, VIBRATION_PRO).firstNotNullOfOrNull(service::getCharacteristic)
            return CommonChannel(write, input, service.getCharacteristic(COMMAND_RESPONSE), vibration)
        }
    }
}
