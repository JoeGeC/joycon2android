package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalLayout
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.LayoutFamily
import com.joegec.joycon2android.buttonmapping.MappingLayout
import com.joegec.joycon2android.buttonmapping.SavedLayout
import com.joegec.joycon2android.buttonmapping.preset.ClassicControllerMapping
import com.joegec.joycon2android.buttonmapping.preset.AndroidMapping
import com.joegec.joycon2android.buttonmapping.preset.GameCubeMapping
import com.joegec.joycon2android.buttonmapping.preset.WasdMouseMapping
import com.joegec.joycon2android.buttonmapping.preset.JoyconWiiMapping
import com.joegec.joycon2android.buttonmapping.preset.MappingPreset
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.buttonmapping.preset.MarioKartNunchukMapping
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWheelMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProSidewaysMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProSidewaysTriggersMapping
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.DropdownOption

/** Resolved in composition, the only place resources can be read. Saved layouts carry their own name. */
class LayoutLabels internal constructor(
    private val names: Map<MappingPreset, String>,
    private val descriptions: Map<MappingPreset, String>,
    private val playerDescriptions: Map<MappingPreset, String>,
    private val families: Map<LayoutFamily, String>,
) {
    fun name(layout: MappingLayout): String =
        if (layout is SavedLayout) layout.name else names.getValue(layout as MappingPreset)

    fun name(layout: GlobalLayout): String = layout.name

    fun name(family: LayoutFamily): String = families.getValue(family)

    fun description(layout: MappingLayout): String? = descriptions[layout]

    fun playerDescription(layout: MappingLayout): String? = playerDescriptions[layout] ?: description(layout)
}

@Composable
internal fun rememberLayoutLabels(): LayoutLabels {
    val presets = Console.entries.flatMap(MappingPresets::forConsole).distinct()
    return LayoutLabels(
        names = presets.associateWith { nameOf(it) },
        descriptions = presets.mapNotNull { preset -> descriptionOf(preset)?.let { preset to it } }.toMap(),
        playerDescriptions = presets.mapNotNull { preset -> playerDescriptionOf(preset)?.let { preset to it } }.toMap(),
        families = LayoutFamily.entries.associateWith { nameOf(it) },
    )
}

@Composable
private fun nameOf(preset: MappingPreset): String = when (preset) {
    GameCubeMapping, SwitchProMapping, AndroidMapping -> stringResource(R.string.layout_standard)
    WasdMouseMapping -> stringResource(R.string.layout_wasd_mouse)
    WiiMapping -> stringResource(R.string.layout_wii)
    JoyconWiiMapping -> stringResource(R.string.layout_joycon)
    MarioKartWheelMapping -> stringResource(R.string.layout_mario_kart_wheel)
    MarioKartNunchukMapping -> stringResource(R.string.layout_mario_kart_nunchuk)
    ClassicControllerMapping -> stringResource(R.string.layout_classic_controller)
    SwitchProSidewaysMapping -> stringResource(R.string.layout_pro_controller)
    SwitchProSidewaysTriggersMapping -> stringResource(R.string.layout_pro_controller_triggers)
}

@Composable
private fun descriptionOf(preset: MappingPreset): String? = when (preset) {
    GameCubeMapping, SwitchProMapping, AndroidMapping -> null
    WasdMouseMapping -> stringResource(R.string.layout_wasd_mouse_description)
    WiiMapping -> stringResource(R.string.layout_wii_description)
    JoyconWiiMapping -> stringResource(R.string.layout_joycon_description)
    MarioKartWheelMapping -> stringResource(R.string.layout_mario_kart_wheel_description)
    MarioKartNunchukMapping -> stringResource(R.string.layout_mario_kart_nunchuk_description)
    ClassicControllerMapping -> stringResource(R.string.layout_classic_controller_description)
    SwitchProSidewaysMapping -> stringResource(R.string.layout_pro_controller_description)
    SwitchProSidewaysTriggersMapping -> stringResource(R.string.layout_pro_controller_triggers_description)
}

@Composable
private fun playerDescriptionOf(preset: MappingPreset): String? = when (preset) {
    SwitchProSidewaysMapping -> stringResource(R.string.layout_pro_controller_player_description)
    SwitchProSidewaysTriggersMapping -> stringResource(R.string.layout_pro_controller_triggers_player_description)
    else -> null
}

@Composable
private fun nameOf(family: LayoutFamily): String = when (family) {
    LayoutFamily.MARIO_KART -> stringResource(R.string.layout_family_mario_kart)
}

internal fun LayoutLabels.option(layout: MappingLayout) = option(layout, description(layout))

internal fun LayoutLabels.playerOption(layout: MappingLayout) = option(layout, playerDescription(layout))

private fun LayoutLabels.option(layout: MappingLayout, subLabel: String?) = DropdownOption(
    id = layout.id,
    label = name(layout),
    subLabel = subLabel,
    deletable = layout is SavedLayout,
)

fun LayoutLabels.sessionName(global: GlobalMapping): String? =
    global.matchingSaved?.let(::name)
        ?: global.sharedLayout?.let(::name)
        ?: global.sharedSavedName
        ?: global.sharedFamily?.let(::name)
