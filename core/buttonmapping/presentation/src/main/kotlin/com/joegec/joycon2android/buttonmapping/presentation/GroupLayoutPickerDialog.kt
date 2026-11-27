package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.OptionListDialog

/** The group layout on its own, for picking outside the mapping editor. */
@Composable
fun GroupLayoutPickerDialog(
    state: ControllerMappingUiState,
    warnUnsavedMapping: Boolean,
    onSelect: (layoutId: String) -> Unit,
    onStopWarningUnsavedMapping: () -> Unit,
    onDismiss: () -> Unit,
) {
    val choice = groupLayoutChoice(state.console, state.global, state.savedLayouts, rememberLayoutLabels())
    val anyCustom = state.players.any { it.layout == null }
    var replacing by remember { mutableStateOf<String?>(null) }

    replacing?.let { layoutId ->
        UnsavedMappingDialog(
            group = true,
            onConfirm = { stopWarning ->
                if (stopWarning) onStopWarningUnsavedMapping()
                onSelect(layoutId)
            },
            onDismiss = { replacing = null },
        )
        return
    }

    OptionListDialog(
        title = stringResource(R.string.controller_mapping_group_layout),
        options = choice.options.filter { it.available },
        selectedId = choice.selectedId,
        dismissLabel = stringResource(R.string.controller_mapping_cancel),
        onSelect = { layoutId -> if (warnUnsavedMapping && anyCustom) replacing = layoutId else onSelect(layoutId) },
        onDismiss = onDismiss,
    )
}
