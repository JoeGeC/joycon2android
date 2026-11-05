package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.emittedFor
import com.joegec.joycon2android.buttonmapping.physicalButtons
import com.joegec.joycon2android.buttonmapping.target.PadButton
import com.joegec.joycon2android.buttonmapping.target.PadStick
import com.joegec.joycon2android.model.JoyconButton

/** Each button on the pad button it's printed as, so the pad sends what it did before it could be remapped. */
object AndroidMapping : MappingPreset {
    override val id = "STANDARD"
    override val console = Console.ANDROID

    override fun entries(side: JoyconSide) = buttons(side).buttonEntries() + sticks(side).stickEntries()

    internal fun buttons(side: JoyconSide): Map<PadButton, JoyconButton> =
        side.physicalButtons().mapNotNull { button ->
            val emitted = button.emittedFor(side) ?: return@mapNotNull null
            PadButton.entries.firstOrNull { it.slot == emitted }?.let { it to button }
        }.toMap()

    // A sideways Joy-Con's stick is the pad's left stick.
    private fun sticks(side: JoyconSide): Map<PadStick, StickSource> = when (side) {
        JoyconSide.DUAL, JoyconSide.GAMECUBE -> mapOf(PadStick.LStick to StickSource.LEFT_STICK, PadStick.RStick to StickSource.RIGHT_STICK)
        JoyconSide.LEFT -> mapOf(PadStick.LStick to StickSource.LEFT_STICK)
        JoyconSide.RIGHT -> mapOf(PadStick.LStick to StickSource.RIGHT_STICK)
    }
}
