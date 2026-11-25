package com.joegec.joycon2android.notification

import android.content.Context
import com.joegec.joycon2android.R
import com.joegec.joycon2android.model.ControllerModel

class ControllerNames(private val context: Context) {

    fun name(model: ControllerModel, deviceName: String): String = when (model) {
        ControllerModel.JOYCON_LEFT -> context.getString(R.string.notification_controller_joycon_left)
        ControllerModel.JOYCON_RIGHT -> context.getString(R.string.notification_controller_joycon_right)
        ControllerModel.PRO_CONTROLLER -> context.getString(R.string.notification_controller_pro)
        ControllerModel.GAMECUBE -> context.getString(R.string.notification_controller_gamecube)
        ControllerModel.UNKNOWN -> deviceName
    }
}
