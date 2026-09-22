package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.MappingLayout

/** A layout the app ships: what each body maps to before the user overrides anything. */
sealed interface MappingPreset : MappingLayout {
    val console: Console
}
