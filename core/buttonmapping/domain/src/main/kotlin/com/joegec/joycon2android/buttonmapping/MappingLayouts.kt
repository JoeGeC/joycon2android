package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPresets

/** The layouts one body can choose between: the console's shipped ones, then the user's own. */
object MappingLayouts {

    fun forBody(console: Console, side: JoyconSide, saved: List<SavedLayout>): List<MappingLayout> =
        MappingPresets.forConsole(console) + saved.filter { it.console == console && it.side == side }

    /**
     * What applying [layout] leaves behind: its own bindings over the console's default ones, so a
     * target no layout mentions — one a later build adds, say — still arrives bound to something.
     */
    fun entriesOf(console: Console, side: JoyconSide, layout: MappingLayout): Map<String, String> =
        MappingPresets.default(console).entries(side) + layout.entries(side)

    /**
     * A layout is recognised by what it says, never by a choice remembered against it: a body reads
     * as a layout whenever its bindings *are* that layout's, whoever set them. So deleting a layout
     * takes away its name and nothing else, and those same bindings read as it again the day an
     * identical layout is saved back. Null is the editor's "Custom".
     */
    fun matching(
        console: Console,
        side: JoyconSide,
        entries: Map<String, String>,
        sidewaysRemote: Boolean,
        saved: List<SavedLayout>,
    ): MappingLayout? = forBody(console, side, saved).firstOrNull {
        it.sidewaysRemote == sidewaysRemote && entriesOf(console, side, it) == entries
    }

    /** Falls back to the console's default for an id whose layout has since been deleted. */
    fun byId(console: Console, side: JoyconSide, id: String?, saved: List<SavedLayout>): MappingLayout =
        forBody(console, side, saved).firstOrNull { it.id == id } ?: MappingPresets.default(console)

    /** Ids the user chose, so a saved layout can never collide with a shipped one. */
    fun newId(): String = "saved-${java.util.UUID.randomUUID()}"

    /** The first "<prefix> N" nothing already answers to, so a suggested name is never a duplicate. */
    fun nextName(prefix: String, taken: Collection<String>): String =
        generateSequence(1) { it + 1 }.map { "$prefix $it" }.first { it !in taken }
}
