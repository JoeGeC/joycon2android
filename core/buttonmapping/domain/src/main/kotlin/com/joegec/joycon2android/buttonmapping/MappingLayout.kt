package com.joegec.joycon2android.buttonmapping

/** Shipped or saved; entries are in the repository's string form. docs/architecture.md#button-mapping */
interface MappingLayout {
    val id: String

    /** docs/dsu-motion.md#playing-as-a-sideways-wii-remote */
    val sidewaysRemote: Boolean get() = false

    fun entries(side: JoyconSide): Map<String, String>
}
