package com.joegec.joycon2android.buttonmapping.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.semantics.Role
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.ConfirmDialog
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/** [onConfirm] carries the checkbox; cancelling changes nothing, so it can't turn the warning off by accident. */
@Composable
internal fun UnsavedMappingDialog(
    group: Boolean,
    onConfirm: (stopWarning: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var stopWarning by remember { mutableStateOf(false) }
    ConfirmDialog(
        title = stringResource(R.string.controller_mapping_unsaved_title),
        body = stringResource(if (group) R.string.controller_mapping_unsaved_group_body else R.string.controller_mapping_unsaved_body),
        confirmLabel = stringResource(R.string.controller_mapping_unsaved_confirm),
        dismissLabel = stringResource(R.string.controller_mapping_cancel),
        onConfirm = { onConfirm(stopWarning) },
        onDismiss = onDismiss,
    ) {
        // AlertDialog keeps 24 dp under its text, so the row sits lower to even out the gaps.
        Row(
            Modifier
                .fillMaxWidth()
                .offset(y = Dimens.elementSpacing)
                .padding(top = Dimens.elementSpacing)
                .toggleable(value = stopWarning, role = Role.Checkbox, onValueChange = { stopWarning = it }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        ) {
            Checkbox(
                checked = stopWarning,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = Accent),
            )
            Text(
                stringResource(R.string.controller_mapping_unsaved_stop),
                color = TextDim,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
