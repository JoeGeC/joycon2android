package com.joegec.joycon2android.emulator

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice

private const val PREFIX = "Joy-Con Virtual Gamepad "

/**
 * Maps each assigned player number to the device "port" an emulator sees for its virtual gamepad.
 * Our pads share a guid, so emulators tell them apart by enumeration rank among same-guid devices —
 * which isn't the player number (the pads enumerate in creation order). We read the live input-device
 * list, find our pads by name, and rank them by device id to reproduce that order.
 */
fun virtualGamepadPorts(context: Context): Map<Int, Int> {
    val ports = HashMap<Int, Int>()
    virtualPads(context).forEachIndexed { rank, device ->
        playerOf(device)?.let { player -> ports[player] = rank }
    }
    return ports
}

/**
 * Maps each assigned player number to the id Dolphin uses in its `Android/<id>/<name>` device
 * qualifier. Dolphin takes that id from `InputDevice.getControllerNumber()` — Android's own gamepad
 * enumeration counter — falling back to a duplicate-name index only for non-gamepads, so it cannot
 * be derived from the player number: any built-in controller (Odin, Thor) already holds number 1.
 */
fun virtualGamepadControllerNumbers(context: Context): Map<Int, Int> {
    val numbers = HashMap<Int, Int>()
    virtualPads(context).forEach { device ->
        playerOf(device)?.let { player -> numbers[player] = device.controllerNumber }
    }
    return numbers
}

private fun virtualPads(context: Context): List<InputDevice> {
    val manager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager ?: return emptyList()
    val deviceIds = manager.inputDeviceIds ?: return emptyList()
    return deviceIds.toList()
        .mapNotNull { id -> manager.getInputDevice(id) }
        .filter { device -> device.name.startsWith(PREFIX) }
        .sortedBy { device -> device.id }
}

private fun playerOf(device: InputDevice): Int? =
    device.name.removePrefix(PREFIX).trim().toIntOrNull()
