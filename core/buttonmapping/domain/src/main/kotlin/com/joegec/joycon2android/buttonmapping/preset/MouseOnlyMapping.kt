package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.StickDirection
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.targetKey
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.MouseAction

object MouseOnlyMapping : MappingPreset {
    override val id = "MOUSE_ONLY"
    override val console = Console.PC

    override fun entries(side: JoyconSide): Map<String, String> =
        mouse(side).entries.associate { (action, source) -> action.targetKey to source.id }

    private fun mouse(side: JoyconSide): Map<MouseAction, MappingSource> = when (side) {
        JoyconSide.DUAL, JoyconSide.GAMECUBE -> clicks(JoyconButton.ZR, JoyconButton.ZL, JoyconButton.RS) +
            scroll(StickSource.RIGHT_STICK)
        JoyconSide.LEFT -> clicks(JoyconButton.ZL, JoyconButton.L, JoyconButton.LS) + scroll(StickSource.LEFT_STICK)
        JoyconSide.RIGHT -> clicks(JoyconButton.ZR, JoyconButton.R, JoyconButton.RS) + scroll(StickSource.RIGHT_STICK)
    }

    private fun clicks(left: JoyconButton, right: JoyconButton, middle: JoyconButton) = mapOf(
        MouseAction.LEFT_CLICK to MappingSource.Button(left),
        MouseAction.RIGHT_CLICK to MappingSource.Button(right),
        MouseAction.MIDDLE_CLICK to MappingSource.Button(middle),
    )

    private fun scroll(stick: StickSource) = mapOf(
        MouseAction.SCROLL_UP to MappingSource.Stick(stick, StickDirection.UP),
        MouseAction.SCROLL_DOWN to MappingSource.Stick(stick, StickDirection.DOWN),
    )
}
