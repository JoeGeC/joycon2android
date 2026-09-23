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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.JoyconSide
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.components.DropdownOption
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
                LayoutRow(
                    options = state.layouts.map { labels.option(it) },
                    selectedId = state.layout?.id,
                    layoutName = state.layout?.let(labels::name),
                    subLabel = state.layout?.let(labels::description),
                    onSelect = { actions.selectLayout(state.body, it) },
                    onSave = onSaveLayout,
                    onDelete = onDeleteLayout,
                )
                if (state.offersSidewaysRemote) {
                    SidewaysRemoteSwitch(state.body.side, state.sidewaysRemote) {
                        actions.setSidewaysRemote(state.body, it)
                    }
                }
                MappingBindings(console, state, actions)
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
            ControllerChip(R.string.controller_pro, player.left!!)
        } else {
            player.left?.let { ControllerChip(R.string.controller_left, it) }
            player.right?.let { ControllerChip(R.string.controller_right, it) }
        }
    }
}

@Composable
private fun ControllerChip(textRes: Int, joycon: ConnectedJoycon) {
    Text(
        stringResource(textRes),
        color = joyconBorderColor(joycon.accentColor, JoyconDefaultColor),
        style = MaterialTheme.typography.titleMedium,
    )
}

/** A right Joy-Con aims from its tail: docs/dsu-motion.md#playing-as-a-sideways-wii-remote */
@Composable
private fun SidewaysRemoteSwitch(side: JoyconSide, enabled: Boolean, onSetEnabled: (Boolean) -> Unit) {
    val aimsFromItsTail = side == JoyconSide.RIGHT
    SettingSwitch(
        title = stringResource(R.string.controller_mapping_sideways_remote),
        description = stringResource(
            if (aimsFromItsTail) R.string.controller_mapping_sideways_remote_description_right
            else R.string.controller_mapping_sideways_remote_description_left,
        ),
        warning = stringResource(R.string.controller_mapping_sideways_remote_warning)
            .takeIf { aimsFromItsTail },
        checked = enabled,
        onCheckedChange = onSetEnabled,
    )
}
