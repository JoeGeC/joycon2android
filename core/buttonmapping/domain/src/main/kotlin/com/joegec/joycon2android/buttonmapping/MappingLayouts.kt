package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPreset
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets

object MappingLayouts {

    fun forBody(console: Console, side: JoyconSide, saved: List<SavedLayout>): List<MappingLayout> =
        MappingPresets.forConsole(console).filter { side in it.sides } +
            saved.filter { it.console == console && it.side == side }

    /** Presets some held body fits; the rest still get the family's other grip when one is chosen. */
    fun forGroup(console: Console, sides: Collection<JoyconSide>): List<MappingPreset> =
        MappingPresets.forConsole(console).filter { preset -> sides.any { it in preset.sides } }

    /** Clears every target first, as the console default fills any the stored mapping omits. */
    fun entriesOf(console: Console, side: JoyconSide, layout: MappingLayout): Map<String, String> =
        MappingPresets.default(console).entries(side).mapValues { "" } + mouseDefaults(side) + layout.entries(side)

    /** Matched by bindings, never a stored choice: docs/architecture.md#button-mapping. Null is "Custom". */
    fun matching(
        console: Console,
        side: JoyconSide,
        entries: Map<String, String>,
        motion: RemoteMotion,
        saved: List<SavedLayout>,
    ): MappingLayout? = forBody(console, side, saved).firstOrNull {
        it.motion(side) == motion && entriesOf(console, side, it).bound() == entries.bound()
    }

    /** Against the layout it differs from least, the earliest on a tie, so the console default wins one. */
    fun changesFromClosest(
        console: Console,
        side: JoyconSide,
        entries: Map<String, String>,
        motion: RemoteMotion,
        layouts: List<MappingLayout>,
    ): MappingChanges = layouts
        .map { changesFrom(it, console, side, entries, motion) }
        .minByOrNull { it.count }
        ?: MappingChanges()

    private fun changesFrom(
        layout: MappingLayout,
        console: Console,
        side: JoyconSide,
        entries: Map<String, String>,
        motion: RemoteMotion,
    ): MappingChanges {
        val theirs = entriesOf(console, side, layout).bound()
        val ours = entries.bound()
        val base = layout.motion(side)
        return MappingChanges(
            targets = (theirs.keys + ours.keys).filterTo(mutableSetOf()) { theirs[it] != ours[it] },
            controller = base.controller != motion.controller,
            thrust = base.thrust != motion.thrust,
            sensitivity = base.sensitivityPercent != motion.sensitivityPercent,
        )
    }

    // A target cleared back to "None" is stored as empty, which a layout that never bound it omits.
    private fun Map<String, String>.bound() = filterValues { it.isNotEmpty() }

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

    /** Saved layouts sharing a name are one game on different bodies. Empty when [id] isn't saved. */
    fun namedAlike(saved: List<SavedLayout>, console: Console, id: String): List<SavedLayout> {
        val name = saved.firstOrNull { it.id == id && it.console == console }?.name ?: return emptyList()
        return saved.filter { it.console == console && it.name == name }
    }

    /** Prefixed so it can never collide with a shipped id. */
    fun newId(): String = "saved-${java.util.UUID.randomUUID()}"

    fun nextName(prefix: String, taken: Collection<String>): String =
        generateSequence(1) { it + 1 }.map { "$prefix $it" }.first { it !in taken }
}
