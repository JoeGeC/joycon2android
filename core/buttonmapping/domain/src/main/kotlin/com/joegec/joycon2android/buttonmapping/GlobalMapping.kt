package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPreset

/**
 * The session read as one setting. It agrees only while every player does — on a saved set whose
 * bindings they all still carry, on a single layout every one of them reads as, or on one family of
 * layouts they are each on their own body's grip of. Change one player and it agrees on nothing.
 *
 * Which of those it is, rather than what to call it: the naming is presentation's.
 */
data class GlobalMapping(
    val players: List<PlayerMapping>,
    val savedLayouts: List<GlobalLayout>,
) {
    val bodies: List<PlayerBody> get() = players.map { it.body }

    val matchingSaved: GlobalLayout?
        get() = savedLayouts.firstOrNull { it.bodies == players.map(PlayerMapping::snapshot) }

    val sharedLayout: MappingLayout?
        get() = players.takeIf { it.isNotEmpty() }?.map { it.layout }?.distinct()?.singleOrNull()

    /** A table rarely holds the same thing, so one family across two grips still agrees. */
    val sharedFamily: LayoutFamily?
        get() = players.takeIf { it.isNotEmpty() }
            ?.map { (it.layout as? MappingPreset)?.family }
            ?.distinct()
            ?.singleOrNull()

    val selectedId: String? get() = matchingSaved?.id ?: sharedLayout?.id
}
