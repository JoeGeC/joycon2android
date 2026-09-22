package com.joegec.joycon2android.buttonmapping

/** One player's mapping as the editor sees it: what their body is bound to, and what that amounts to. */
data class PlayerMapping(
    val body: PlayerBody,
    val entries: Map<String, String>,
    val sidewaysRemote: Boolean,
    /** The layout these bindings *are*; null once they are no layout's, which the editor calls Custom. */
    val layout: MappingLayout?,
) {
    fun snapshot() = PlayerLayoutSnapshot(body, entries, sidewaysRemote)
}
