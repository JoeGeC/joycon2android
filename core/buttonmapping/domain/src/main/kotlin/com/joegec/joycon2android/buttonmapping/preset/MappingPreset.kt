package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.buttonmapping.LayoutFamily
import com.joegec.joycon2android.buttonmapping.MappingLayout

/** A layout the app ships: what each body maps to before the user overrides anything. */
sealed interface MappingPreset : MappingLayout {
    val console: Console

    /** The bodies it is offered to — a grip that only one of them can be held in says so. */
    val sides: Set<JoyconSide> get() = JoyconSide.entries.toSet()

    /** The same game in another grip; see [LayoutFamily]. */
    val family: LayoutFamily? get() = null
}
