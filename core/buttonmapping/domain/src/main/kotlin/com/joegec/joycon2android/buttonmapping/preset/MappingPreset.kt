package com.joegec.joycon2android.buttonmapping.preset

import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide

/**
 * A named layout a console can start from: what each body maps to before the user overrides
 * anything. Entries are in the repository's opaque string form, so a preset and a stored override
 * are the same kind of value.
 */
sealed interface MappingPreset {
    val id: String
    val displayName: String
    val console: Console

    /**
     * Whether this layout stands a lone Joy-Con in for a Wii Remote held sideways, the way a game
     * written for that grip expects one. Its motion turns onto the sideways remote's frame and its
     * d-pad turns with it; a pair, held like a remote already, is untouched.
     */
    val sidewaysRemote: Boolean get() = false

    fun entries(side: JoyconSide): Map<String, String>
}
