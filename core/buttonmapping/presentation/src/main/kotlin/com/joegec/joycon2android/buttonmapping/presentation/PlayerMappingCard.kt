package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.roundToInt
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.RemoteMotion
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.SettingSlider
import com.joegec.joycon2android.ui.components.SettingSwitch
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.JoyconDefaultColor
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.joyconBorderColor

@Composable
fun PlayerMappingCard(
    console: Console,
    player: PlayerState,
    state: PlayerMappingUiState,
    labels: LayoutLabels,
    actions: MappingActions,
    onSaveLayout: () -> Unit,
    onDeleteLayout: (DropdownOption) -> Unit,
    modifier: Modifier = Modifier,
    deviceKeyWarnings: DeviceKeyWarnings = DeviceKeyWarnings(),
) {
    var expanded by rememberSaveable(state.body) { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.cardCorner))
            .background(CardBg),
    ) {
        CardHeader(player, state.layout?.let(labels::name), expanded) { expanded = !expanded }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                Modifier.padding(
                    start = Dimens.compactRowPaddingHorizontal,
                    end = Dimens.compactRowPaddingHorizontal,
                    bottom = Dimens.cardPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
            ) {
                PlayerLayoutPicker(state, labels, actions, onSaveLayout, onDeleteLayout)
                if (state.offersProController) {
                    ProControllerSwitch(state.proController) { actions.setProController(state.body, it) }
                }
                if (state.wiiControllers.isNotEmpty()) {
                    WiiControllerChoice(state.body.side, state.wiiControllers, state.wiiController) {
                        actions.setWiiController(state.body, it)
                    }
                }
                if (state.offersThrust) ThrustSwitch(state.thrust) { actions.setThrust(state.body, it) }
                if (state.offersSensitivity) {
                    SensitivitySlider(state.sensitivityPercent) { actions.setSensitivity(state.body, it) }
                }
                MappingBindings(console, state, actions, deviceKeyWarnings)
            }
        }
    }
}

@Composable
private fun CardHeader(player: PlayerState, layoutName: String?, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(
                horizontal = Dimens.compactRowPaddingHorizontal,
                vertical = Dimens.compactRowPaddingVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        Text(
            stringResource(R.string.player_label, player.player.index),
            color = Accent,
            style = MaterialTheme.typography.titleMedium,
        )
        ControllerChips(player)
        Text(
            layoutName ?: stringResource(R.string.controller_mapping_layout_custom),
            color = TextDim,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null,
            tint = TextDim,
            modifier = Modifier.size(Dimens.iconSizeSmall),
        )
    }
}

@Composable
private fun ControllerChips(player: PlayerState) {
    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.compactControllerGap)) {
        if (player.hasPro) {
            ControllerChip(player.left!!.modelLabel(), player.left!!)
        } else {
            player.left?.let { ControllerChip(stringResource(R.string.controller_left), it) }
            player.right?.let { ControllerChip(stringResource(R.string.controller_right), it) }
        }
    }
}

@Composable
private fun ControllerChip(text: String, joycon: ConnectedJoycon) {
    Text(
        text,
        color = joyconBorderColor(joycon.accentColor, JoyconDefaultColor),
        style = MaterialTheme.typography.titleMedium,
    )
}

@Composable
private fun PlayerLayoutPicker(
    state: PlayerMappingUiState,
    labels: LayoutLabels,
    actions: MappingActions,
    onSaveLayout: () -> Unit,
    onDeleteLayout: (DropdownOption) -> Unit,
) {
    val layoutName = state.layout?.let(labels::name)
    Column(Modifier.padding(bottom = Dimens.mappingSwitchBottomPadding)) {
        LayoutPanel(title = stringResource(R.string.controller_mapping_player_layout)) {
            LayoutRow(
                options = state.layouts.map { labels.playerOption(it) },
                selectedId = state.layout?.id,
                layoutName = layoutName,
                onSelect = { actions.selectLayout(state.body, it) },
                onSave = onSaveLayout,
                onDelete = onDeleteLayout,
            )
        }
        SaveLayoutHint(
            visible = layoutName == null,
            text = stringResource(R.string.controller_mapping_save_player_hint),
        )
    }
}

/** docs/virtual-gamepad.md#single-joy-con */
@Composable
private fun ProControllerSwitch(enabled: Boolean, onSetEnabled: (Boolean) -> Unit) {
    SettingSwitch(
        title = stringResource(R.string.controller_mapping_pro_controller),
        description = stringResource(R.string.controller_mapping_pro_controller_description),
        checked = enabled,
        onCheckedChange = onSetEnabled,
        modifier = Modifier.padding(bottom = Dimens.mappingSwitchBottomPadding),
    )
}

/** docs/dsu-motion.md#thrusts */
@Composable
private fun ThrustSwitch(enabled: Boolean, onSetEnabled: (Boolean) -> Unit) {
    SettingSwitch(
        title = stringResource(R.string.controller_mapping_thrust),
        description = stringResource(R.string.controller_mapping_thrust_description),
        checked = enabled,
        onCheckedChange = onSetEnabled,
        modifier = Modifier.padding(bottom = Dimens.mappingSwitchBottomPadding),
    )
}

/** Held locally while dragging, so the thumb doesn't wait on storage. docs/dsu-motion.md#motion-sensitivity */
@Composable
private fun SensitivitySlider(percent: Int, onSetPercent: (Int) -> Unit) {
    var dragged by remember(percent) { mutableFloatStateOf(percent.toFloat()) }
    val range = RemoteMotion.SENSITIVITY_PERCENT_RANGE
    SettingSlider(
        title = stringResource(R.string.controller_mapping_sensitivity),
        description = stringResource(R.string.controller_mapping_sensitivity_description),
        valueLabel = stringResource(R.string.controller_mapping_sensitivity_value, dragged.roundToInt()),
        value = dragged,
        valueRange = range.first.toFloat()..range.last.toFloat(),
        steps = (range.last - range.first) / RemoteMotion.SENSITIVITY_PERCENT_STEP - 1,
        onValueChange = { dragged = it },
        onValueChangeFinished = { onSetPercent(dragged.roundToInt()) },
        modifier = Modifier.padding(bottom = Dimens.mappingSwitchBottomPadding),
    )
}
