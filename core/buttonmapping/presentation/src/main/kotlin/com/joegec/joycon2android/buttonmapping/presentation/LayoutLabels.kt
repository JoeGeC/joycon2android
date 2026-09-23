package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalLayout
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.LayoutFamily
import com.joegec.joycon2android.buttonmapping.MappingLayout
import com.joegec.joycon2android.buttonmapping.SavedLayout
import com.joegec.joycon2android.buttonmapping.preset.GameCubeMapping
import com.joegec.joycon2android.buttonmapping.preset.JoyconWiiMapping
import com.joegec.joycon2android.buttonmapping.preset.MappingPreset
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.buttonmapping.preset.MarioKartNunchukMapping
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWheelMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProMapping
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.DropdownOption

/** Resolved in composition, the only place resources can be read. Saved layouts carry their own name. */
class LayoutLabels internal constructor(
    private val names: Map<MappingPreset, String>,
    private val descriptions: Map<MappingPreset, String>,
    private val families: Map<LayoutFamily, String>,
) {
    fun name(layout: MappingLayout): String =
        if (layout is SavedLayout) layout.name else names.getValue(layout as MappingPreset)

    fun name(layout: GlobalLayout): String = layout.name

    fun name(family: LayoutFamily): String = families.getValue(family)

    fun description(layout: MappingLayout): String? = descriptions[layout]
}

@Composable
internal fun rememberLayoutLabels(): LayoutLabels {
    val presets = Console.entries.flatMap(MappingPresets::forConsole).distinct()
    return LayoutLabels(
        names = presets.associateWith { nameOf(it) },
        descriptions = presets.mapNotNull { preset -> descriptionOf(preset)?.let { preset to it } }.toMap(),
        families = LayoutFamily.entries.associateWith { nameOf(it) },
    )
}

@Composable
private fun nameOf(preset: MappingPreset): String = when (preset) {
    GameCubeMapping, SwitchProMapping -> stringResource(R.string.layout_standard)
    WiiMapping -> stringResource(R.string.layout_wii)
    JoyconWiiMapping -> stringResource(R.string.layout_joycon)
    MarioKartWheelMapping -> stringResource(R.string.layout_mario_kart_wheel)
    MarioKartNunchukMapping -> stringResource(R.string.layout_mario_kart_nunchuk)
}

@Composable
private fun descriptionOf(preset: MappingPreset): String? = when (preset) {
    GameCubeMapping, SwitchProMapping -> null // the only layout their console offers
    WiiMapping -> stringResource(R.string.layout_wii_description)
    JoyconWiiMapping -> stringResource(R.string.layout_joycon_description)
    MarioKartWheelMapping -> stringResource(R.string.layout_mario_kart_wheel_description)
    MarioKartNunchukMapping -> stringResource(R.string.layout_mario_kart_nunchuk_description)
}

@Composable
private fun nameOf(family: LayoutFamily): String = when (family) {
    LayoutFamily.MARIO_KART -> stringResource(R.string.layout_family_mario_kart)
}

internal fun LayoutLabels.option(layout: MappingLayout) = DropdownOption(
    id = layout.id,
    label = name(layout),
    subLabel = description(layout),
    deletable = layout is SavedLayout,
)

fun LayoutLabels.sessionName(global: GlobalMapping): String? =
    global.matchingSaved?.let(::name)
        ?: global.sharedLayout?.let(::name)
        ?: global.sharedFamily?.let(::name)
