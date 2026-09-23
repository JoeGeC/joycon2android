package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPreset
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets

object MappingLayouts {

    fun forBody(console: Console, side: JoyconSide, saved: List<SavedLayout>): List<MappingLayout> =
        MappingPresets.forConsole(console).filter { side in it.sides } +
            saved.filter { it.console == console && it.side == side }

    /** Layered over the console default, so a target the layout omits is still bound. */
    fun entriesOf(console: Console, side: JoyconSide, layout: MappingLayout): Map<String, String> =
        MappingPresets.default(console).entries(side) + layout.entries(side)

    /** Matched by bindings, never a stored choice: docs/architecture.md#button-mapping. Null is "Custom". */
    fun matching(
        console: Console,
        side: JoyconSide,
        entries: Map<String, String>,
        sidewaysRemote: Boolean,
        saved: List<SavedLayout>,
    ): MappingLayout? = forBody(console, side, saved).firstOrNull {
        it.sidewaysRemote == sidewaysRemote && entriesOf(console, side, it) == entries
    }

    /** Falls back to the family's other grip, then the console default (as a deleted layout does). */
    fun byId(console: Console, side: JoyconSide, id: String?, saved: List<SavedLayout>): MappingLayout =
        forBody(console, side, saved).firstOrNull { it.id == id }
            ?: familyMember(console, side, id)
            ?: MappingPresets.default(console)

    private fun familyMember(console: Console, side: JoyconSide, id: String?): MappingPreset? {
        val presets = MappingPresets.forConsole(console)
        val family = presets.firstOrNull { it.id == id }?.family ?: return null
        return presets.firstOrNull { it.family == family && side in it.sides }
    }

    /** Prefixed so it can never collide with a shipped id. */
    fun newId(): String = "saved-${java.util.UUID.randomUUID()}"

    fun nextName(prefix: String, taken: Collection<String>): String =
        generateSequence(1) { it + 1 }.map { "$prefix $it" }.first { it !in taken }
}
