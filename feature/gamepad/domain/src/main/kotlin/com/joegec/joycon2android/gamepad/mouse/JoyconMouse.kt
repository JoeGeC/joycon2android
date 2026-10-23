package com.joegec.joycon2android.gamepad.mouse

import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconInput
import com.joegec.joycon2android.model.Side
import kotlin.math.abs

/** One Joy-Con 2 read as a mouse while it rests on a surface: docs/virtual-gamepad.md#mouse */
class JoyconMouse(private val side: Side) {

    private var lastX: Int? = null
    private var lastY: Int? = null
    private var lastPacketId: Int? = null
    private var wheelDetents = 0f

    /**
     * Null while the Joy-Con is lifted, held, or has no mouse sensor. [defaultControls] adds the
     * fixed clicks and scroll, for a player whose mapping has none of its own.
     */
    fun read(input: JoyconInput, defaultControls: Boolean = true): MouseFrame? {
        val sensor = input.mouse?.takeIf { it.onSurface && standsOnRail(input) } ?: return null.also { forget() }
        val frame = MouseFrame(
            dx = sensor.x.since(lastX),
            dy = sensor.y.since(lastY),
            wheel = if (defaultControls) wheel(input) else 0.also { wheelDetents = 0f },
            buttons = if (defaultControls) buttons(input.pressed) else emptySet(),
        )
        lastX = sensor.x
        lastY = sensor.y
        lastPacketId = input.packetId
        return frame
    }

    // Fingers round the rail read as a surface too; only a desk turns the rail to the floor.
    private fun standsOnRail(input: JoyconInput): Boolean {
        val awayFromRail = when (side) {
            Side.LEFT -> -input.accelX
            Side.RIGHT -> input.accelX
            Side.PRO, Side.UNKNOWN -> return false
        }
        return awayFromRail >= RAIL_DOWN_ACCEL
    }

    private fun forget() {
        lastX = null
        lastY = null
        lastPacketId = null
        wheelDetents = 0f
    }

    // The counters are int16 and wrap.
    private fun Int.since(last: Int?): Int = if (last == null) 0 else (this - last).toShort().toInt()

    private fun wheel(input: JoyconInput): Int {
        val tilt = ((input.stickY - STICK_CENTER) / STICK_RANGE.toFloat()).coerceIn(-1f, 1f)
        if (abs(tilt) < WHEEL_DEAD_ZONE) {
            wheelDetents = 0f
            return 0
        }
        wheelDetents += tilt * DETENTS_PER_SECOND * elapsedMs(input.packetId) / 1000f
        val whole = wheelDetents.toInt()
        wheelDetents -= whole
        return whole
    }

    // The packet id is a millisecond clock: docs/protocol.md#packet-layout
    private fun elapsedMs(packetId: Int): Int {
        val last = lastPacketId ?: return 0
        return ((packetId - last) and PACKET_ID_MASK).coerceAtMost(MAX_ELAPSED_MS)
    }

    companion object {
        private const val STICK_CENTER = 2048
        private const val STICK_RANGE = 2047
        private const val WHEEL_DEAD_ZONE = 0.25f
        private const val DETENTS_PER_SECOND = 20f
        private const val PACKET_ID_MASK = 0xFFFFFF
        private const val MAX_ELAPSED_MS = 100

        // Raw accel reads +1 g (4096) on the axis pointing up; this allows a 45° lean.
        private const val RAIL_DOWN_ACCEL = 2896

        private val PRIMARY = setOf(JoyconButton.L.id, JoyconButton.R.id)
        private val SECONDARY = setOf(JoyconButton.ZL.id, JoyconButton.ZR.id)

        /** What a Joy-Con on a surface spends on the mouse, so it never also reaches a game. */
        fun withheldFrom(input: JoyconInput): JoyconInput =
            input.copy(pressed = input.pressed - PRIMARY - SECONDARY, stickX = STICK_CENTER, stickY = STICK_CENTER)

        private fun buttons(pressed: Set<String>): Set<MouseButton> = buildSet {
            if (pressed.any { it in PRIMARY }) add(MouseButton.PRIMARY)
            if (pressed.any { it in SECONDARY }) add(MouseButton.SECONDARY)
        }
    }
}
