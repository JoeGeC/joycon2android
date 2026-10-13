package com.joegec.joycon2android.assignment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.ControllerModel
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.OptionDropdown

private const val AUTO_ID = "AUTO"
private val choosableModels = ControllerModel.entries - ControllerModel.UNKNOWN

@Composable
internal fun ControllerTypeDropdown(
    joycon: ConnectedJoycon,
    onSetType: (ControllerModel?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val auto = stringResource(R.string.type_auto, joycon.detectedModel.label())
    val options = listOf(DropdownOption(AUTO_ID, auto)) +
        choosableModels.map { DropdownOption(it.name, it.label()) }
    OptionDropdown(
        options = options,
        selectedId = joycon.typeOverride?.name ?: AUTO_ID,
        label = joycon.typeOverride?.label() ?: auto,
        subLabel = stringResource(R.string.type_hint),
        onSelect = { id -> onSetType(if (id == AUTO_ID) null else ControllerModel.valueOf(id)) },
        modifier = modifier,
    )
}
