package com.joegec.joycon2android.emulator

import android.content.Context
import android.hardware.input.InputManager
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent

/*
 * Resolves how each emulator identifies our virtual gamepads, by reading the live input-device
 * list rather than deriving a number from the player index.
 *
 * Every emulator picks its own quantity, and none of them is the player number, so each rule is
 * read from the emulator's own source and mirrored here:
 *
 * - **Dolphin** takes the id in its `Android/<id>/<name>` qualifier from
 *   `InputDevice.getControllerNumber()` — Android's gamepad enumeration counter.
 *   `ControllerInterface::AddDevice` prefers `GetPreferredId()`, which the Android backend fills
 *   from `getControllerNumber()`, falling back to a duplicate-name index only for non-gamepads.
 * - **Eden** (yuzu lineage) numbers `port` by walking `InputDevice.getDeviceIds()` and counting
 *   *every* physical game controller it passes, so any built-in pad shifts ours along. See
 *   `InputHandler.getDevices()`: a controller number already registered is skipped but still
 *   consumes a port, which edenGamepadPorts reproduces.
 *
 * A guessed number binds a config to the wrong device or to none, and any handheld with a built-in
 * controller already occupies the low numbers — hence read, never derive.
 */

private const val PREFIX = "Joy-Con Virtual Gamepad "

/** Each assigned player number to the `port` Eden expects for its virtual gamepad. */
fun edenGamepadPorts(context: Context): Map<Int, Int> {
    val ports = HashMap<Int, Int>()
    val registered = HashSet<Int>()
    var port = 0
    inputDevices(context).forEach { device ->
        if (!isPhysicalGameController(device)) return@forEach
        if (registered.add(device.controllerNumber)) {
            playerOf(device)?.let { player -> ports[player] = port }
        }
        port++
    }
    return ports
}

/** Each assigned player number to the id Dolphin expects in its `Android/<id>/<name>` qualifier. */
fun dolphinGamepadIds(context: Context): Map<Int, Int> {
    val ids = HashMap<Int, Int>()
    inputDevices(context).forEach { device ->
        playerOf(device)?.let { player -> ids[player] = device.controllerNumber }
    }
    return ids
}

// Eden walks the ids in the order the platform returns them, so this must not be re-sorted.
private fun inputDevices(context: Context): List<InputDevice> {
    val manager = context.getSystemService(Context.INPUT_SERVICE) as? InputManager ?: return emptyList()
    val deviceIds = manager.inputDeviceIds ?: return emptyList()
    return deviceIds.toList().mapNotNull { id -> manager.getInputDevice(id) }
}

// Mirrors Eden's InputHandler.isPhysicalGameController.
private fun isPhysicalGameController(device: InputDevice): Boolean {
    if (device.isVirtual) return false

    val sources = device.sources
    val hasControllerSource = sources and InputDevice.SOURCE_GAMEPAD == InputDevice.SOURCE_GAMEPAD ||
        sources and InputDevice.SOURCE_JOYSTICK == InputDevice.SOURCE_JOYSTICK
    if (!hasControllerSource) return false

    val hasControllerButtons = device.hasKeys(*CONTROLLER_BUTTONS).any { it }
    val hasControllerAxes = device.motionRanges.any { range -> range.axis in CONTROLLER_AXES }
    return hasControllerButtons || hasControllerAxes
}

private fun playerOf(device: InputDevice): Int? =
    if (device.name.startsWith(PREFIX)) device.name.removePrefix(PREFIX).trim().toIntOrNull() else null

private val CONTROLLER_BUTTONS = intArrayOf(
    KeyEvent.KEYCODE_BUTTON_A,
    KeyEvent.KEYCODE_BUTTON_B,
    KeyEvent.KEYCODE_BUTTON_X,
    KeyEvent.KEYCODE_BUTTON_Y,
    KeyEvent.KEYCODE_BUTTON_L1,
    KeyEvent.KEYCODE_BUTTON_R1,
    KeyEvent.KEYCODE_BUTTON_L2,
    KeyEvent.KEYCODE_BUTTON_R2,
    KeyEvent.KEYCODE_BUTTON_THUMBL,
    KeyEvent.KEYCODE_BUTTON_THUMBR,
    KeyEvent.KEYCODE_BUTTON_START,
    KeyEvent.KEYCODE_BUTTON_SELECT,
    KeyEvent.KEYCODE_DPAD_UP,
    KeyEvent.KEYCODE_DPAD_DOWN,
    KeyEvent.KEYCODE_DPAD_LEFT,
    KeyEvent.KEYCODE_DPAD_RIGHT,
)

private val CONTROLLER_AXES = intArrayOf(
    MotionEvent.AXIS_X,
    MotionEvent.AXIS_Y,
    MotionEvent.AXIS_Z,
    MotionEvent.AXIS_RX,
    MotionEvent.AXIS_RY,
    MotionEvent.AXIS_RZ,
    MotionEvent.AXIS_HAT_X,
    MotionEvent.AXIS_HAT_Y,
    MotionEvent.AXIS_LTRIGGER,
    MotionEvent.AXIS_RTRIGGER,
)
