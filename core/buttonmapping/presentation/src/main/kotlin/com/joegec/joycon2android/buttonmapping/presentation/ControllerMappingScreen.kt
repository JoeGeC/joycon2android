package com.joegec.joycon2android.buttonmapping.presentation

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.buttonmapping.MappingLayouts
import com.joegec.joycon2android.buttonmapping.PlayerBody
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.model.PlayerState
import com.joegec.joycon2android.ui.components.ConfirmDialog
import com.joegec.joycon2android.ui.components.TextInputDialog
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

@Composable
fun ControllerMappingScreen(
    state: ControllerMappingUiState,
    players: List<PlayerState>,
    actions: MappingActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    var dialog by remember { mutableStateOf<MappingDialog?>(null) }

    Column(
        modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = Dimens.screenPaddingHorizontal),
    ) {
        ScreenHeader(state, onBack)
        Spacer(Modifier.height(Dimens.sectionSpacing))
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Dimens.sectionSpacing),
        ) {
            if (state.players.isEmpty()) {
                Text(stringResource(R.string.controller_mapping_no_players), color = TextDim)
            } else {
                AllPlayersRow(state.global, actions) { dialog = it }
            }
            state.players.forEach { player ->
                val connected = players.firstOrNull { it.player == player.body.player }
                if (connected != null) {
                    PlayerMappingCard(
                        console = state.console,
                        player = connected,
                        state = player,
                        actions = actions,
                        onSaveLayout = { dialog = MappingDialog.Save(player.body) },
                        onDeleteLayout = { dialog = MappingDialog.Delete(it.id, it.label, global = false) },
                    )
                }
            }
            Spacer(Modifier.height(Dimens.sectionSpacing))
        }
    }

    MappingDialogs(state, dialog, actions) { dialog = null }
}

@Composable
private fun ScreenHeader(state: ControllerMappingUiState, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.controller_mapping_back),
            )
        }
        Text(state.console.displayName, style = MaterialTheme.typography.headlineSmall, color = Color.White)
    }
}

/** The session read as one setting, so a whole table can be set — and kept — in a single move. */
@Composable
private fun AllPlayersRow(
    state: GlobalLayoutUiState,
    actions: MappingActions,
    onDialog: (MappingDialog) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.controller_mapping_all_players),
            color = TextDim,
            modifier = Modifier.weight(1f),
        )
        LayoutRow(
            options = state.options,
            selectedId = state.selectedId,
            layoutName = state.layoutName,
            subLabel = state.playerSummary,
            onSelect = actions.selectGlobalLayout,
            onSave = { onDialog(MappingDialog.Save(body = null)) },
            onDelete = { onDialog(MappingDialog.Delete(it.id, it.label, global = true)) },
            modifier = Modifier.weight(1f),
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
                if (dialog.body == null) state.global.savedNames else state.savedLayoutNames,
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
                actions.deleteLayout(dialog.id, dialog.global)
                onDismiss()
            },
            onDismiss = onDismiss,
        )
    }
}

private sealed interface MappingDialog {
    /** A null body names the session as a whole rather than one player. */
    data class Save(val body: PlayerBody?) : MappingDialog

    data class Delete(val id: String, val name: String, val global: Boolean) : MappingDialog
}
