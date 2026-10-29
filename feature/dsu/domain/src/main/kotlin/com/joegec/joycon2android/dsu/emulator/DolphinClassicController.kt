package com.joegec.joycon2android.dsu.emulator

import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.MappingSource
import com.joegec.joycon2android.buttonmapping.WiiController
import com.joegec.joycon2android.buttonmapping.target.ClassicButton
import com.joegec.joycon2android.buttonmapping.target.ClassicStick
import com.joegec.joycon2android.buttonmapping.toSourceMap
import com.joegec.joycon2android.buttonmapping.toStickDirectionMap
import com.joegec.joycon2android.emulatorconfig.DolphinControls

/** The remote's lines once its extension is a Classic Controller: docs/dsu-motion.md#classic-controller */
internal object DolphinClassicController {

    private val DOLPHIN_KEYS = mapOf(
        ClassicButton.ClassicA to "Classic/Buttons/A",
        ClassicButton.ClassicB to "Classic/Buttons/B",
        ClassicButton.ClassicX to "Classic/Buttons/X",
        ClassicButton.ClassicY to "Classic/Buttons/Y",
        ClassicButton.ClassicL to "Classic/Triggers/L",
        ClassicButton.ClassicR to "Classic/Triggers/R",
        ClassicButton.ClassicZL to "Classic/Buttons/ZL",
        ClassicButton.ClassicZR to "Classic/Buttons/ZR",
        ClassicButton.ClassicPlus to "Classic/Buttons/+",
        ClassicButton.ClassicMinus to "Classic/Buttons/-",
        ClassicButton.ClassicHome to "Classic/Buttons/Home",
        ClassicButton.ClassicDPadUp to "Classic/D-Pad/Up",
        ClassicButton.ClassicDPadDown to "Classic/D-Pad/Down",
        ClassicButton.ClassicDPadLeft to "Classic/D-Pad/Left",
        ClassicButton.ClassicDPadRight to "Classic/D-Pad/Right",
        ClassicButton.ClassicRecenter to "IMUIR/Recenter",
    )

    private val STICK_GROUPS = mapOf(
        ClassicStick.ClassicLeftStick to "Classic/Left Stick",
        ClassicStick.ClassicRightStick to "Classic/Right Stick",
    )

    private val SIDEWAYS = WiiController.CLASSIC.heldSideways

    fun lines(side: JoyconSide, mapping: Map<String, String>): List<String> =
        listOf("Extension = Classic") + buttonLines(side, mapping) + stickLines(side, mapping)

    /** Shake isn't a line of its own: it rides the remote's accelerometer, as the remote's does. */
    fun shake(mapping: Map<String, String>): List<MappingSource>? = mapping.toSourceMap<ClassicButton>()[ClassicButton.ClassicShake]

    private fun buttonLines(side: JoyconSide, mapping: Map<String, String>): List<String> =
        (mapping.toSourceMap<ClassicButton>() - ClassicButton.ClassicShake).mapNotNull { (target, sources) ->
            DolphinDsuInputs.expressionFor(side, SIDEWAYS, sources)?.let { "${DOLPHIN_KEYS.getValue(target)} = $it" }
        }

    private fun stickLines(side: JoyconSide, mapping: Map<String, String>): List<String> =
        mapping.toStickDirectionMap<ClassicStick>().flatMap { (stick, directions) ->
            directions.mapNotNull { (direction, sources) ->
                DolphinDsuInputs.expressionFor(side, SIDEWAYS, sources)?.let {
                    "${STICK_GROUPS.getValue(stick)}/${DolphinControls.DIRECTIONS.getValue(direction)} = $it"
                }
            }
        }
}
