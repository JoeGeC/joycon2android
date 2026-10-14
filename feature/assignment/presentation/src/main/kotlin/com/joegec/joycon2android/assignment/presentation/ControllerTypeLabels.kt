package com.joegec.joycon2android.assignment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel

@Composable
internal fun ControllerModel.label(): String = when (this) {
    ControllerModel.JOYCON_LEFT -> stringResource(R.string.type_joycon_left)
    ControllerModel.JOYCON_RIGHT -> stringResource(R.string.type_joycon_right)
    ControllerModel.PRO_CONTROLLER -> stringResource(R.string.type_pro)
    ControllerModel.GAMECUBE -> stringResource(R.string.type_gamecube)
    ControllerModel.UNKNOWN -> stringResource(R.string.type_unknown)
}

@Composable
internal fun ConnectedJoycon.modelLabel(): String = when (model) {
    ControllerModel.JOYCON_LEFT -> stringResource(R.string.side_left)
    ControllerModel.JOYCON_RIGHT -> stringResource(R.string.side_right)
    ControllerModel.PRO_CONTROLLER -> stringResource(R.string.side_pro)
    ControllerModel.GAMECUBE -> stringResource(R.string.side_gamecube)
    ControllerModel.UNKNOWN -> deviceName
}
