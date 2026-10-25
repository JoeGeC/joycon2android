package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.WiiController
import com.joegec.joycon2android.core.buttonmapping.presentation.R

@Composable
internal fun WiiController.label(): String = when (this) {
    WiiController.REMOTE -> stringResource(R.string.wii_controller_remote)
    WiiController.SIDEWAYS_REMOTE -> stringResource(R.string.wii_controller_sideways_remote)
    WiiController.CLASSIC -> stringResource(R.string.wii_controller_classic)
}

@Composable
internal fun WiiController.description(): String = when (this) {
    WiiController.REMOTE -> stringResource(R.string.wii_controller_remote_description)
    WiiController.SIDEWAYS_REMOTE -> stringResource(R.string.wii_controller_sideways_remote_description)
    WiiController.CLASSIC -> stringResource(R.string.wii_controller_classic_description)
}
