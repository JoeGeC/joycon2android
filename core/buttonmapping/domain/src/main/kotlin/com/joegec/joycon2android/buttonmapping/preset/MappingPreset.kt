package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.LayoutFamily
import com.joegec.joycon2android.buttonmapping.MappingLayout

sealed interface MappingPreset : MappingLayout {
    val console: Console

    val sides: Set<JoyconSide> get() = JoyconSide.entries.toSet()

    /** The same game in another grip; see [LayoutFamily]. */
    val family: LayoutFamily? get() = null
}
