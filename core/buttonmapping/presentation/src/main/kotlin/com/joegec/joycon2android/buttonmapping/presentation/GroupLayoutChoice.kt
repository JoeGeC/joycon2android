package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.MappingLayouts
import com.joegec.joycon2android.buttonmapping.SavedLayout
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.DropdownOption

internal data class GroupLayoutChoice(
    val options: List<DropdownOption>,
    val selectedId: String?,
    /** Player layouts by name; one option sets every body saved under it. */
    val named: Map<String, List<SavedLayout>>,
)

@Composable
internal fun groupLayoutChoice(
    console: Console,
    global: GlobalMapping,
    playerLayouts: List<SavedLayout>,
    labels: LayoutLabels,
): GroupLayoutChoice {
    val named = playerLayouts.groupBy { it.name }
    val forBodies = named.map { (name, layouts) ->
        val sides = layouts.map { it.side }.distinct()
        DropdownOption(
            id = layouts.first().id,
            label = name,
            subLabel = stringResource(R.string.controller_mapping_body_layout, sides.map { it.shortLabel() }.joinToString(", ")),
            available = global.bodies.any { it.side in sides },
            deletable = true,
        )
    }
    val saved = global.savedLayouts.map {
        DropdownOption(
            id = it.id,
            label = labels.name(it),
            subLabel = it.playerSummary(),
            available = it.fits(global.bodies),
            deletable = true,
        )
    }
    val shipped = MappingLayouts.forGroup(console, global.bodies.map { it.side }).map { labels.option(it) }
    return GroupLayoutChoice(
        options = shipped + forBodies + saved,
        selectedId = global.matchingSaved?.id
            ?: global.sharedSavedName?.let { named[it]?.first()?.id }
            ?: global.sharedLayout?.id,
        named = named,
    )
}
