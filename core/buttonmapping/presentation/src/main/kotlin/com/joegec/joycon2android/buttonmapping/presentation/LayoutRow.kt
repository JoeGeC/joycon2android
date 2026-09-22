package com.joegec.joycon2android.buttonmapping.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.core.buttonmapping.presentation.R
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.OptionDropdown
import com.joegec.joycon2android.ui.theme.Accent
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/**
 * Picks the layout a body — or the whole session — follows, and offers to keep what it has become.
 * The name reads "Custom" the moment the bindings stop matching a layout, and reads a layout's own
 * name again the moment they match one.
 */
@Composable
fun LayoutRow(
    options: List<DropdownOption>,
    selectedId: String?,
    layoutName: String?,
    onSelect: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: (DropdownOption) -> Unit,
    modifier: Modifier = Modifier,
    subLabel: String? = null,
) {
    val context = LocalContext.current

    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OptionDropdown(
            options = options,
            selectedId = selectedId,
            label = layoutName ?: stringResource(R.string.controller_mapping_layout_custom),
            subLabel = subLabel,
            onSelect = onSelect,
            onDelete = onDelete,
            onUnavailable = { option ->
                context.toast(
                    R.string.controller_mapping_layout_unavailable,
                    option.label,
                    option.subLabel.orEmpty(),
                )
            },
            modifier = Modifier.weight(1f),
        )
        SaveButton(layoutName, onSave)
    }
}

/**
 * Only a mapping with no name of its own is worth naming: one that already reads as a layout has
 * been saved once already, so the icon dims and says which layout it is rather than making a twin.
 */
@Composable
private fun SaveButton(layoutName: String?, onSave: () -> Unit) {
    val context = LocalContext.current

    IconButton(
        onClick = {
            if (layoutName == null) onSave()
            else context.toast(R.string.controller_mapping_layout_already_saved, layoutName)
        },
    ) {
        Icon(
            Icons.Filled.Save,
            contentDescription = stringResource(R.string.controller_mapping_save_layout),
            tint = if (layoutName == null) Accent else TextDim,
            modifier = Modifier.size(Dimens.iconSizeMedium),
        )
    }
}

private fun Context.toast(messageRes: Int, vararg args: String) =
    Toast.makeText(this, getString(messageRes, *args), Toast.LENGTH_LONG).show()
