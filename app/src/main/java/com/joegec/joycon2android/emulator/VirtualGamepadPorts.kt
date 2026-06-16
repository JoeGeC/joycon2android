package com.joegec.joycon2android.emulator

import android.content.Context
import android.hardware.input.InputManager

private const val PREFIX = "Joy-Con Virtual Gamepad "

/**
 * Maps each assigned player number to the device "port" an emulator sees for its virtual gamepad.
 * Our pads share a guid, so emulators tell them apart by enumeration rank among same-guid devices —
 * which isn't the player number (the pads enumerate in creation order). We read the live input-device
 * list, find our pads by name, and rank them by device id to reproduce that order.
 */
fun virtualGamepadPorts(context: Context): Map<Int, Int> {
    val manager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager ?: return emptyMap()
    val deviceIds = manager.inputDeviceIds ?: return emptyMap()
    val pads = deviceIds.toList()
        .mapNotNull { id -> manager.getInputDevice(id) }
        .filter { device -> device.name.startsWith(PREFIX) }
        .sortedBy { device -> device.id }

    val ports = HashMap<Int, Int>()
    pads.forEachIndexed { rank, device ->
        device.name.removePrefix(PREFIX).trim().toIntOrNull()?.let { player -> ports[player] = rank }
    }
    return ports
}
