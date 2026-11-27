package com.joegec.joycon2android.buttonmapping.presentation

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalLayout
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.LayoutFamily
import com.joegec.joycon2android.buttonmapping.MappingLayout
import com.joegec.joycon2android.buttonmapping.SavedLayout
import com.joegec.joycon2android.buttonmapping.preset.ClassicControllerMapping
import com.joegec.joycon2android.buttonmapping.preset.AndroidMapping
import com.joegec.joycon2android.buttonmapping.preset.AndroidXboxMapping
import com.joegec.joycon2android.buttonmapping.preset.OtherEmulatorMapping
import com.joegec.joycon2android.buttonmapping.preset.GameCubeMapping
import com.joegec.joycon2android.buttonmapping.preset.WasdMouseMapping
import com.joegec.joycon2android.buttonmapping.preset.JoyconWiiMapping
import com.joegec.joycon2android.buttonmapping.preset.MappingPreset
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.buttonmapping.preset.MarioKartNunchukMapping
import com.joegec.joycon2android.buttonmapping.preset.MarioKartWheelMapping
import com.joegec.joycon2android.buttonmapping.preset.MouseOnlyMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProSidewaysMapping
import com.joegec.joycon2android.buttonmapping.preset.SwitchProSidewaysTriggersMapping
import com.joegec.joycon2android.buttonmapping.preset.WiiMapping
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.DropdownOption

/** Saved layouts carry their own name. */
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
    val resources = LocalContext.current.resources
    return remember(LocalConfiguration.current) { layoutLabels(resources) }
}

fun layoutLabels(resources: Resources): LayoutLabels {
    val presets = Console.entries.flatMap(MappingPresets::forConsole).distinct()
    return LayoutLabels(
        names = presets.associateWith { resources.getString(nameOf(it)) },
        descriptions = presets.mapNotNull { preset -> descriptionOf(preset)?.let { preset to resources.getString(it) } }.toMap(),
        playerDescriptions = presets.mapNotNull { preset -> playerDescriptionOf(preset)?.let { preset to resources.getString(it) } }.toMap(),
        families = LayoutFamily.entries.associateWith { resources.getString(nameOf(it)) },
    )
}

@StringRes
private fun nameOf(preset: MappingPreset): Int = when (preset) {
    GameCubeMapping, SwitchProMapping, OtherEmulatorMapping -> R.string.layout_standard
    AndroidMapping -> R.string.layout_as_labelled
    AndroidXboxMapping -> R.string.layout_xbox
    WasdMouseMapping -> R.string.layout_wasd_mouse
    MouseOnlyMapping -> R.string.layout_mouse_only
    WiiMapping -> R.string.layout_wii
    JoyconWiiMapping -> R.string.layout_joycon
    MarioKartWheelMapping -> R.string.layout_mario_kart_wheel
    MarioKartNunchukMapping -> R.string.layout_mario_kart_nunchuk
    ClassicControllerMapping -> R.string.layout_classic_controller
    SwitchProSidewaysMapping -> R.string.layout_pro_controller
    SwitchProSidewaysTriggersMapping -> R.string.layout_pro_controller_triggers
}

@StringRes
private fun descriptionOf(preset: MappingPreset): Int? = when (preset) {
    GameCubeMapping, SwitchProMapping, OtherEmulatorMapping -> null
    AndroidMapping -> R.string.layout_as_labelled_description
    AndroidXboxMapping -> R.string.layout_xbox_description
    WasdMouseMapping -> R.string.layout_wasd_mouse_description
    MouseOnlyMapping -> R.string.layout_mouse_only_description
    WiiMapping -> R.string.layout_wii_description
    JoyconWiiMapping -> R.string.layout_joycon_description
    MarioKartWheelMapping -> R.string.layout_mario_kart_wheel_description
    MarioKartNunchukMapping -> R.string.layout_mario_kart_nunchuk_description
    ClassicControllerMapping -> R.string.layout_classic_controller_description
    SwitchProSidewaysMapping -> R.string.layout_pro_controller_description
    SwitchProSidewaysTriggersMapping -> R.string.layout_pro_controller_triggers_description
}

@StringRes
private fun playerDescriptionOf(preset: MappingPreset): Int? = when (preset) {
    SwitchProSidewaysMapping -> R.string.layout_pro_controller_player_description
    SwitchProSidewaysTriggersMapping -> R.string.layout_pro_controller_triggers_player_description
    else -> null
}

@StringRes
private fun nameOf(family: LayoutFamily): Int = when (family) {
    LayoutFamily.MARIO_KART -> R.string.layout_family_mario_kart
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
