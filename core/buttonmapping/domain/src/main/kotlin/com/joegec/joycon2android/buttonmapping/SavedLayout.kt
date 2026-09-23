package com.joegec.joycon2android.buttonmapping

data class SavedLayout(
    override val id: String,
    val name: String,
    val console: Console,
    val side: JoyconSide,
    val bindings: Map<String, String>,
    override val sidewaysRemote: Boolean = false,
) : MappingLayout {
    /** Only ever offered back to the body it was saved from, so [side] is always that one. */
    override fun entries(side: JoyconSide) = bindings
}
