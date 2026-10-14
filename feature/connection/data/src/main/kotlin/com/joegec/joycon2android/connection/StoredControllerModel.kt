package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel

// A side saved by an earlier build, or a model the menu can't choose, reads as no override.
internal object StoredControllerModel {
    fun parse(value: Any?): ControllerModel? =
        ControllerModel.entries.firstOrNull { it.name == value && it != ControllerModel.UNKNOWN }
}
