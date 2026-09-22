package com.joegec.joycon2android.buttonmapping

/**
 * A named set of bindings a body can be set to: one the app ships
 * ([com.joegec.joycon2android.buttonmapping.preset.MappingPreset]) or one the user saved
 * ([SavedLayout]). Entries are in the repository's opaque string form, so a layout and a stored
 * override are the same kind of value.
 *
 * What a shipped one is *called* is not here — that is copy, and it lives in presentation's
 * resources, keyed by the layout itself.
 */
interface MappingLayout {
    val id: String

    /**
     * Whether this layout stands a lone Joy-Con in for a Wii Remote held sideways, the way a game
     * written for that grip expects one. Its motion turns onto the sideways remote's frame and its
     * d-pad turns with it; a pair, held like a remote already, is untouched.
     */
    val sidewaysRemote: Boolean get() = false

    fun entries(side: JoyconSide): Map<String, String>
}
