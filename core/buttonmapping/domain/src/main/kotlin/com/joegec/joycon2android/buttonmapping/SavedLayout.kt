package com.joegec.joycon2android.buttonmapping

/** A layout the user saved from one player's body, offered back to any player holding that body. */
data class SavedLayout(
    override val id: String,
    val name: String,
    val console: Console,
    val side: JoyconSide,
    val bindings: Map<String, String>,
    override val sidewaysRemote: Boolean = false,
) : MappingLayout {
    /** Saved from one body and only ever offered back to it, so [side] is already the side asked for. */
    override fun entries(side: JoyconSide) = bindings
}
