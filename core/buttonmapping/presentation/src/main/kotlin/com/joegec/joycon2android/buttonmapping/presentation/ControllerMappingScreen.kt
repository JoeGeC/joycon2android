package com.joegec.joycon2android.buttonmapping.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.Console
import com.joegec.joycon2android.buttonmapping.GlobalMapping
import com.joegec.joycon2android.buttonmapping.MappingLayouts
import com.joegec.joycon2android.buttonmapping.SavedLayout
import com.joegec.joycon2android.buttonmapping.preset.MappingPresets
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.InfoBox
import com.joegec.joycon2android.ui.components.LocalTips
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.components.ConfirmDialog
import com.joegec.joycon2android.ui.components.TextInputDialog
import com.joegec.joycon2android.ui.theme.CardBg
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim
import com.joegec.joycon2android.ui.theme.TextTitle

@Composable
fun ControllerMappingScreen(
    state: ControllerMappingUiState,
    players: List<PlayerState>,
    actions: MappingActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onInfoClick: (() -> Unit)? = null,
    deviceKeyWarnings: DeviceKeyWarnings = DeviceKeyWarnings(),
) {
    BackHandler(onBack = onBack)
    var dialog by remember { mutableStateOf<MappingDialog?>(null) }
    val labels = rememberLayoutLabels()

    Column(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = Dimens.screenPaddingHorizontal),
    ) {
        ScreenHeader(state, onBack, onInfoClick)
        Spacer(Modifier.height(Dimens.sectionSpacing))
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
        ) {
            val tips = LocalTips.current
            AnimatedVisibility(
                visible = tips.visible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                InfoBox(stringResource(R.string.controller_mapping_tips), onClick = tips.onTap)
            }
            if (state.players.isEmpty()) {
                Text(stringResource(R.string.controller_mapping_no_players), color = TextDim)
            } else {
                GroupLayoutRow(state.console, state.global, state.savedLayouts, labels, actions) { dialog = it }
            }
            state.players.forEach { player ->
                val connected = players.firstOrNull { it.player == player.body.player }
                if (connected != null) {
                    PlayerMappingCard(
                        console = state.console,
                        player = connected,
                        state = player,
                        labels = labels,
                        actions = actions,
                        onSaveLayout = { dialog = MappingDialog.Save(player.body) },
                        onDeleteLayout = { dialog = MappingDialog.Delete(listOf(it.id), it.label, global = false) },
                        deviceKeyWarnings = deviceKeyWarnings,
                    )
                }
            }
            Spacer(Modifier.height(Dimens.sectionSpacing))
        }
    }

    MappingDialogs(state, dialog, actions) { dialog = null }
}

@Composable
private fun ScreenHeader(state: ControllerMappingUiState, onBack: () -> Unit, onInfoClick: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.controller_mapping_back),
            )
        }
        Text(
            state.console.label(),
            style = MaterialTheme.typography.headlineSmall,
            color = TextTitle,
            modifier = Modifier.weight(1f),
        )
        if (onInfoClick != null) {
            IconButton(onClick = onInfoClick) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = stringResource(R.string.controller_mapping_info),
                    tint = TextDim,
                )
            }
        }
    }
}

@Composable
private fun GroupLayoutRow(
    console: Console,
    global: GlobalMapping,
    playerLayouts: List<SavedLayout>,
    labels: LayoutLabels,
    actions: MappingActions,
    onDialog: (MappingDialog) -> Unit,
) {
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

    val layoutName = labels.sessionName(global)

    Column(
        Modifier
            .background(CardBg, RoundedCornerShape(Dimens.buttonCorner))
            .padding(horizontal = Dimens.cardPadding, vertical = Dimens.legendContentVertical),
    ) {
        LayoutPanel(
            title = stringResource(R.string.controller_mapping_group_layout),
            description = stringResource(R.string.controller_mapping_group_layout_description),
        ) {
            LayoutRow(
                options = MappingPresets.forConsole(console).map { labels.option(it) } + forBodies + saved,
                selectedId = global.matchingSaved?.id
                    ?: global.sharedSavedName?.let { named[it]?.first()?.id }
                    ?: global.sharedLayout?.id,
                layoutName = layoutName,
                subLabel = global.matchingSaved?.let { it.playerSummary() },
                onSelect = actions.selectGlobalLayout,
                onSave = { onDialog(MappingDialog.Save(body = null)) },
                onDelete = { option ->
                    val group = global.savedLayouts.any { it.id == option.id }
                    val ids = if (group) listOf(option.id) else named.getValue(option.label).map { it.id }
                    onDialog(MappingDialog.Delete(ids, option.label, global = group))
                },
            )
        }
        SaveLayoutHint(
            visible = layoutName == null,
            text = stringResource(R.string.controller_mapping_save_group_hint),
        )
    }
}

@Composable
private fun MappingDialogs(
    state: ControllerMappingUiState,
    dialog: MappingDialog?,
    actions: MappingActions,
    onDismiss: () -> Unit,
) {
    when (dialog) {
        null -> Unit
        is MappingDialog.Save -> TextInputDialog(
            title = stringResource(R.string.controller_mapping_save_layout),
            fieldLabel = stringResource(R.string.controller_mapping_layout_name),
            defaultValue = MappingLayouts.nextName(
                stringResource(R.string.controller_mapping_layout_custom),
                state.takenNames(dialog.body == null),
            ),
            confirmLabel = stringResource(R.string.controller_mapping_save),
            dismissLabel = stringResource(R.string.controller_mapping_cancel),
            onConfirm = { name ->
                actions.saveLayout(dialog.body, name)
                onDismiss()
            },
            onDismiss = onDismiss,
        )
        is MappingDialog.Delete -> ConfirmDialog(
            title = stringResource(R.string.controller_mapping_delete_layout),
            body = stringResource(R.string.controller_mapping_delete_layout_body, dialog.name),
            confirmLabel = stringResource(R.string.controller_mapping_delete),
            dismissLabel = stringResource(R.string.controller_mapping_cancel),
            onConfirm = {
                dialog.ids.forEach { actions.deleteLayout(it, dialog.global) }
                onDismiss()
            },
            onDismiss = onDismiss,
        )
    }
}

private sealed interface MappingDialog {
    /** Null means the whole session. */
    data class Save(val body: PlayerBody?) : MappingDialog

    /** Several ids when a group option names saved layouts for different bodies. */
    data class Delete(val ids: List<String>, val name: String, val global: Boolean) : MappingDialog
}
