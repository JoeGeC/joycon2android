package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel

@Composable
internal fun ConnectedJoycon.modelLabel(): String = when (model) {
    ControllerModel.JOYCON_LEFT -> stringResource(R.string.controller_left)
    ControllerModel.JOYCON_RIGHT -> stringResource(R.string.controller_right)
    ControllerModel.PRO_CONTROLLER -> stringResource(R.string.controller_pro)
    ControllerModel.GAMECUBE -> stringResource(R.string.controller_gamecube)
    ControllerModel.UNKNOWN -> deviceName
}
