package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPreset

/**
 * The session read as one setting. It has a name only while every player agrees on one — a saved
 * set whose bindings they all still carry, a single layout every one of them reads as, or one
 * family of layouts they are each on their own body's grip of; change one player and the session
 * stops being that thing.
 */
data class GlobalMapping(
    val players: List<PlayerMapping>,
    val savedLayouts: List<GlobalLayout>,
) {
    val bodies: List<PlayerBody> get() = players.map { it.body }

    private val matchingSaved: GlobalLayout?
        get() = savedLayouts.firstOrNull { it.bodies == players.map(PlayerMapping::snapshot) }

    private val sharedLayout: MappingLayout?
        get() = players.takeIf { it.isNotEmpty() }?.map { it.layout }?.distinct()?.singleOrNull()

    /** A table rarely holds the same thing, so one family across two grips still agrees. */
    private val sharedFamily: String?
        get() = players.takeIf { it.isNotEmpty() }
            ?.map { (it.layout as? MappingPreset)?.family }
            ?.distinct()
            ?.singleOrNull()

    val selectedId: String? get() = matchingSaved?.id ?: sharedLayout?.id

    val displayName: String?
        get() = matchingSaved?.displayName ?: sharedLayout?.displayName ?: sharedFamily

    val playerSummary: String? get() = matchingSaved?.playerSummary
}
