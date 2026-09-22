package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.StickSource.LEFT_STICK
import com.joegec.joycon2android.buttonmapping.StickSource.RIGHT_STICK
import com.joegec.joycon2android.buttonmapping.StickSource
import com.joegec.joycon2android.buttonmapping.target.GameCubeButton
import com.joegec.joycon2android.buttonmapping.target.GameCubeStick
import com.joegec.joycon2android.model.JoyconButton
import com.joegec.joycon2android.model.JoyconButton.A
import com.joegec.joycon2android.model.JoyconButton.B
import com.joegec.joycon2android.model.JoyconButton.Capture
import com.joegec.joycon2android.model.JoyconButton.Down
import com.joegec.joycon2android.model.JoyconButton.Home
import com.joegec.joycon2android.model.JoyconButton.L
import com.joegec.joycon2android.model.JoyconButton.Left
import com.joegec.joycon2android.model.JoyconButton.Minus
import com.joegec.joycon2android.model.JoyconButton.Plus
import com.joegec.joycon2android.model.JoyconButton.R
import com.joegec.joycon2android.model.JoyconButton.Right
import com.joegec.joycon2android.model.JoyconButton.SlLeft
import com.joegec.joycon2android.model.JoyconButton.SlRight
import com.joegec.joycon2android.model.JoyconButton.SrLeft
import com.joegec.joycon2android.model.JoyconButton.SrRight
import com.joegec.joycon2android.model.JoyconButton.Up
import com.joegec.joycon2android.model.JoyconButton.X
import com.joegec.joycon2android.model.JoyconButton.Y

object GameCubeMapping : MappingPreset {
    override val id = "STANDARD"
    override val console = Console.GAMECUBE

    override fun entries(side: JoyconSide) = buttons(side).buttonEntries() + sticks(side).stickEntries()

    private fun buttons(side: JoyconSide): Map<GameCubeButton, JoyconButton> = when (side) {
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
            GameCubeButton.Z to Capture,
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

    private fun sticks(side: JoyconSide): Map<GameCubeStick, StickSource> = when (side) {
        JoyconSide.DUAL -> mapOf(
            GameCubeStick.MainStick to LEFT_STICK,
            GameCubeStick.CStick to RIGHT_STICK,
        )
        JoyconSide.LEFT -> mapOf(GameCubeStick.MainStick to LEFT_STICK)
        JoyconSide.RIGHT -> mapOf(GameCubeStick.MainStick to RIGHT_STICK)
    }
}
