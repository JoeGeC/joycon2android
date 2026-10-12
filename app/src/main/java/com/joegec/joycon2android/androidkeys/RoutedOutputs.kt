package com.joegec.joycon2android.androidkeys

import com.joegec.joycon2android.gamepad.GamepadFrame
import com.joegec.joycon2android.model.PlayerState

data class RoutedOutputs(val gamepad: List<GamepadFrame>, val dsu: List<PlayerState>)
