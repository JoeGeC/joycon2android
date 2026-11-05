package com.joegec.joycon2android.gamepad

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.KeyBindings
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.target.PadButton
import com.joegec.joycon2android.buttonmapping.target.PadStick
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.buttonmapping.wholeEmittedStick
import com.joegec.joycon2android.model.GamepadState
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.PlayerState

/** An Android game's mapping, which the app applies as the game can't: docs/virtual-gamepad.md#android-games */
class AndroidPadLayout(
    private val side: JoyconSide,
    private val buttons: KeyBindings<PadButton>,
    private val sticks: Map<PadStick, Map<StickDirection, List<MappingSource>>>,
) {
    fun padFor(state: PlayerState): GamepadState {
        val unmapped = state.gamepad
        val pressed = buttons.heldKeys(state).mapTo(mutableSetOf()) { it.slot.id }
        val (leftX, leftY) = stick(PadStick.LStick, state, unmapped)
        val (rightX, rightY) = stick(PadStick.RStick, state, unmapped)
        return GamepadState(
            pressed = pressed,
            leftStickX = leftX,
            leftStickY = leftY,
            rightStickX = rightX,
            rightStickY = rightY,
            leftTrigger = if (state.hasAnalogTriggers) unmapped.leftTrigger else pulled(JoyconButton.ZL, pressed),
            rightTrigger = if (state.hasAnalogTriggers) unmapped.rightTrigger else pulled(JoyconButton.ZR, pressed),
        )
    }

    // A whole stick stays analog; anything else is pushed to the gate by whichever directions are held.
    private fun stick(target: PadStick, state: PlayerState, unmapped: GamepadState): Pair<Int, Int> {
        val directions = sticks[target].orEmpty()
        when (directions.wholeEmittedStick(side)) {
            StickSource.LEFT_STICK -> return unmapped.leftStickX to unmapped.leftStickY
            StickSource.RIGHT_STICK -> return unmapped.rightStickX to unmapped.rightStickY
            null -> Unit
        }
        val held = KeyBindings(directions).heldKeys(state)
        return axis(StickDirection.LEFT, StickDirection.RIGHT, held) to axis(StickDirection.DOWN, StickDirection.UP, held)
    }

    private fun axis(low: StickDirection, high: StickDirection, held: Set<StickDirection>): Int = when {
        high in held && low !in held -> STICK_MAX
        low in held && high !in held -> 0
        else -> STICK_CENTER
    }

    private fun pulled(trigger: JoyconButton, pressed: Set<String>) = if (trigger.id in pressed) TRIGGER_PULLED else 0

    companion object {
        private const val STICK_CENTER = 2048
        private const val STICK_MAX = 4096
        private const val TRIGGER_PULLED = 255

        fun of(entries: Map<String, String>, side: JoyconSide) =
            AndroidPadLayout(side, KeyBindings(entries.toSourceMap<PadButton>()), entries.toStickDirectionMap<PadStick>())
    }
}
