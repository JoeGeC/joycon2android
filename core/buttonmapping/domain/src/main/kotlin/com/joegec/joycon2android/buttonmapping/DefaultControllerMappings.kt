package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.target.GameCubeButton
import com.joegec.joycon2android.buttonmapping.target.GameCubeStick
import com.joegec.joycon2android.buttonmapping.target.SwitchProButton
import com.joegec.joycon2android.buttonmapping.target.SwitchProStick
import com.joegec.joycon2android.buttonmapping.target.WiimoteButton
import com.joegec.joycon2android.buttonmapping.target.WiimoteStick
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconButton.A
import com.joegec.joycon2android.model.JoyconButton.B
import com.joegec.joycon2android.model.JoyconButton.Camera
import com.joegec.joycon2android.model.JoyconButton.Down
import com.joegec.joycon2android.model.JoyconButton.Home
import com.joegec.joycon2android.model.JoyconButton.L
import com.joegec.joycon2android.model.JoyconButton.LS
import com.joegec.joycon2android.model.JoyconButton.Left
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.Plus
import com.joegec.joycon2android.model.JoyconButton.R
import com.joegec.joycon2android.model.JoyconButton.RS
import com.joegec.joycon2android.model.JoyconButton.Right
import com.joegec.joycon2android.model.JoyconButton.SlLeft
import com.joegec.joycon2android.model.JoyconButton.SlRight
import com.joegec.joycon2android.model.JoyconButton.SrLeft
import com.joegec.joycon2android.model.JoyconButton.SrRight
import com.joegec.joycon2android.model.JoyconButton.Up
import com.joegec.joycon2android.model.JoyconButton.X
import com.joegec.joycon2android.model.JoyconButton.Y
import com.joegec.joycon2android.model.JoyconButton.ZL
import com.joegec.joycon2android.model.JoyconButton.ZR
import com.joegec.joycon2android.buttonmapping.StickSource.LEFT_STICK
import com.joegec.joycon2android.buttonmapping.StickSource.RIGHT_STICK

/**
 * The button/stick assignments this app shipped with before customization existed, transcribed
 * verbatim from each generator's previous hardcoded tables — a fresh install (or any target/body
 * the user has never touched) behaves exactly as it always did.
 */
object DefaultControllerMappings {

    fun gameCubeButtons(side: JoyconSide): Map<GameCubeButton, JoyconButton> = when (side) {
        JoyconSide.DUAL -> mapOf(
            GameCubeButton.A to A,
            GameCubeButton.B to B,
            GameCubeButton.X to X,
            GameCubeButton.Y to Y,
            GameCubeButton.Z to R,
            GameCubeButton.Start to Plus,
            GameCubeButton.TriggerL to L,
            GameCubeButton.TriggerR to R,
            GameCubeButton.DPadUp to Up,
            GameCubeButton.DPadDown to Down,
            GameCubeButton.DPadLeft to Left,
            GameCubeButton.DPadRight to Right,
        )
        JoyconSide.LEFT -> mapOf(
            GameCubeButton.A to Down,
            GameCubeButton.B to Left,
            GameCubeButton.X to Right,
            GameCubeButton.Y to Up,
            GameCubeButton.Z to Camera,
            GameCubeButton.Start to Minus,
            GameCubeButton.TriggerL to SlLeft,
            GameCubeButton.TriggerR to SrLeft,
        )
        JoyconSide.RIGHT -> mapOf(
            GameCubeButton.A to X,
            GameCubeButton.B to A,
            GameCubeButton.X to Y,
            GameCubeButton.Y to B,
            GameCubeButton.Z to Home,
            GameCubeButton.Start to Plus,
            GameCubeButton.TriggerL to SlRight,
            GameCubeButton.TriggerR to SrRight,
        )
    }

    fun gameCubeSticks(side: JoyconSide): Map<GameCubeStick, StickSource> = when (side) {
        JoyconSide.DUAL -> mapOf(
            GameCubeStick.MainStick to RIGHT_STICK,
            GameCubeStick.CStick to LEFT_STICK,
        )
        else -> emptyMap()
    }

    fun switchProButtons(side: JoyconSide): Map<SwitchProButton, JoyconButton> = when (side) {
        JoyconSide.DUAL -> mapOf(
            SwitchProButton.A to A,
            SwitchProButton.B to B,
            SwitchProButton.X to X,
            SwitchProButton.Y to Y,
            SwitchProButton.L to L,
            SwitchProButton.R to R,
            SwitchProButton.ZL to ZL,
            SwitchProButton.ZR to ZR,
            SwitchProButton.Plus to Plus,
            SwitchProButton.Minus to Minus,
            SwitchProButton.Home to Home,
            SwitchProButton.Capture to Camera,
            SwitchProButton.LStickClick to LS,
            SwitchProButton.RStickClick to RS,
            SwitchProButton.DPadUp to Up,
            SwitchProButton.DPadDown to Down,
            SwitchProButton.DPadLeft to Left,
            SwitchProButton.DPadRight to Right,
        )
        JoyconSide.LEFT -> mapOf(
            SwitchProButton.A to Down,
            SwitchProButton.B to Left,
            SwitchProButton.X to Right,
            SwitchProButton.Y to Up,
            SwitchProButton.L to SlLeft,
            SwitchProButton.R to SrLeft,
            SwitchProButton.ZL to L,
            SwitchProButton.ZR to ZL,
            SwitchProButton.Minus to Minus,
            SwitchProButton.LStickClick to LS,
            SwitchProButton.Capture to Camera,
        )
        JoyconSide.RIGHT -> mapOf(
            SwitchProButton.A to X,
            SwitchProButton.B to A,
            SwitchProButton.X to Y,
            SwitchProButton.Y to B,
            SwitchProButton.L to SlRight,
            SwitchProButton.R to SrRight,
            SwitchProButton.ZL to R,
            SwitchProButton.ZR to ZR,
            SwitchProButton.Plus to Plus,
            SwitchProButton.Home to Home,
            SwitchProButton.LStickClick to RS,
        )
    }

    fun switchProSticks(side: JoyconSide): Map<SwitchProStick, StickSource> = when (side) {
        JoyconSide.DUAL -> mapOf(
            SwitchProStick.LStick to LEFT_STICK,
            SwitchProStick.RStick to RIGHT_STICK,
        )
        else -> emptyMap()
    }

    fun wiimoteButtons(side: JoyconSide): Map<WiimoteButton, JoyconButton> = when (side) {
        JoyconSide.DUAL -> mapOf(
            WiimoteButton.A to A,
            WiimoteButton.B to ZR,
            WiimoteButton.One to Y,
            WiimoteButton.Two to B,
            WiimoteButton.Home to Home,
            WiimoteButton.Plus to Plus,
            WiimoteButton.Minus to X,
            WiimoteButton.DPadUp to Up,
            WiimoteButton.DPadDown to Down,
            WiimoteButton.DPadLeft to Left,
            WiimoteButton.DPadRight to Right,
            WiimoteButton.NunchukC to L,
            WiimoteButton.NunchukZ to ZL,
        )
        JoyconSide.LEFT -> mapOf(
            WiimoteButton.A to Down,
            WiimoteButton.B to ZL,
            WiimoteButton.One to Up,
            WiimoteButton.Two to Left,
            WiimoteButton.Home to Camera,
            WiimoteButton.Plus to Right,
            WiimoteButton.Minus to Minus,
        )
        JoyconSide.RIGHT -> mapOf(
            WiimoteButton.A to A,
            WiimoteButton.B to ZR,
            WiimoteButton.One to Y,
            WiimoteButton.Two to B,
            WiimoteButton.Home to Home,
            WiimoteButton.Plus to Plus,
            WiimoteButton.Minus to X,
        )
    }

    fun wiimoteSticks(side: JoyconSide): Map<WiimoteStick, StickSource> = when (side) {
        JoyconSide.DUAL -> mapOf(WiimoteStick.NunchukStick to LEFT_STICK)
        else -> emptyMap()
    }
}
