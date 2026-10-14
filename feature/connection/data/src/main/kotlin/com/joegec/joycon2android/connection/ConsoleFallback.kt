package com.joegec.joycon2android.connection

import com.joegec.joycon2android.model.ControllerModel

// The console report has no trigger bytes: docs/protocol.md#android-workarounds
internal object ConsoleFallback {
    fun allows(model: ControllerModel) = !model.hasAnalogTriggers
}
