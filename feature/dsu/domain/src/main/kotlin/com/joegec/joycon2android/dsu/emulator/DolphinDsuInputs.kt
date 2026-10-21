package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.emittedDirection
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.emittedStick
import com.joegec.joycon2android.model.JoyconButton

/** Dolphin's names for what our DSU server streams, as one expression per target. */
internal object DolphinDsuInputs {

    // DSU carries exactly the DS4 button set — these are protocol input names, not UI copy.
    private val DS4_NAMES = mapOf(
        JoyconButton.A to "Circle",
        JoyconButton.B to "Cross",
        JoyconButton.X to "Triangle",
        JoyconButton.Y to "Square",
        JoyconButton.L to "L1",
        JoyconButton.R to "R1",
        JoyconButton.ZL to "L2",
        JoyconButton.ZR to "R2",
        JoyconButton.Minus to "Share",
        JoyconButton.Plus to "Options",
        JoyconButton.LS to "L3",
        JoyconButton.RS to "R3",
        JoyconButton.Home to "PS",
        JoyconButton.Capture to "Touch Button",
    )

    // The D-Pad target reads the DS4 pad nibble instead of a face button.
    private val PAD_NAMES = mapOf(
        JoyconButton.Up to "Pad N",
        JoyconButton.Down to "Pad S",
        JoyconButton.Left to "Pad W",
        JoyconButton.Right to "Pad E",
    )

    fun expressionFor(side: JoyconSide, sidewaysRemote: Boolean, sources: List<MappingSource>): String? =
        sources.mapNotNull { specFor(side, sidewaysRemote, it) }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" | ") { "`$it`" }

    private fun specFor(side: JoyconSide, sidewaysRemote: Boolean, source: MappingSource): String? = when (source) {
        is MappingSource.Button -> source.button.emittedFor(side)?.let { DS4_NAMES[it] ?: PAD_NAMES[it] }
        is MappingSource.Stick -> tiltSpec(source.emittedStick(side), source.emittedDirection(side, sidewaysRemote))
    }

    // DSU sticks report up as a positive Y, unlike Android's axes.
    private fun tiltSpec(stick: StickSource, direction: StickDirection): String {
        val prefix = if (stick == StickSource.LEFT_STICK) "Left" else "Right"
        return when (direction) {
            StickDirection.UP -> "$prefix Y+"
            StickDirection.DOWN -> "$prefix Y-"
            StickDirection.LEFT -> "$prefix X-"
            StickDirection.RIGHT -> "$prefix X+"
        }
    }
}
