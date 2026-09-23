package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.MappingPreset

/** Agrees only while every player does: on a saved set, one layout, or one [LayoutFamily]. */
data class GlobalMapping(
    val players: List<PlayerMapping>,
    val savedLayouts: List<GlobalLayout>,
) {
    val bodies: List<PlayerBody> get() = players.map { it.body }

    val matchingSaved: GlobalLayout?
        get() = savedLayouts.firstOrNull { it.bodies == players.map(PlayerMapping::snapshot) }

    val sharedLayout: MappingLayout?
        get() = players.takeIf { it.isNotEmpty() }?.map { it.layout }?.distinct()?.singleOrNull()

    val sharedFamily: LayoutFamily?
        get() = players.takeIf { it.isNotEmpty() }
            ?.map { (it.layout as? MappingPreset)?.family }
            ?.distinct()
            ?.singleOrNull()

    val selectedId: String? get() = matchingSaved?.id ?: sharedLayout?.id
}
