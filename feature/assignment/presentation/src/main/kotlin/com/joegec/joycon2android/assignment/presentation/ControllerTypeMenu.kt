package com.joegec.joycon2android.assignment.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.PanelDropdownMenu
import com.joegec.joycon2android.ui.theme.Dimens

private const val DETECT_ID = "DETECT"
private val choosableModels = ControllerModel.entries - ControllerModel.UNKNOWN

@Composable
internal fun ControllerTypeMenu(
    joycon: ConnectedJoycon,
    label: String,
    color: Color,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSetType: (ControllerModel?) -> Unit,
) {
    Box {
        Row(
            Modifier
                .clip(RoundedCornerShape(Dimens.buttonCorner))
                .clickable(onClickLabel = stringResource(R.string.type_change)) { onExpandedChange(true) }
                .heightIn(min = Dimens.minTouchTarget),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, color = color, style = MaterialTheme.typography.titleSmall)
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(Dimens.iconSizeMedium),
            )
        }
        PanelDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            options = typeOptions(joycon),
            selectedId = joycon.typeOverride?.name ?: DETECT_ID,
            onSelect = { option ->
                onExpandedChange(false)
                onSetType(if (option.id == DETECT_ID) null else ControllerModel.valueOf(option.id))
            },
        )
    }
}

@Composable
private fun typeOptions(joycon: ConnectedJoycon): List<DropdownOption> {
    val detect = DropdownOption(
        DETECT_ID,
        stringResource(R.string.type_detect),
        subLabel = joycon.detectedModel.label(),
    )
    return listOf(detect) + choosableModels.map { DropdownOption(it.name, it.label()) }
}
