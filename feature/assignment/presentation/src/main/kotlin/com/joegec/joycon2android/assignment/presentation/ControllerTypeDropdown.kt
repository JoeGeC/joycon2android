package com.joegec.joycon2android.assignment.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.model.ConnectedJoycon
import com.joegec.joycon2android.model.Side
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.OptionDropdown

private const val AUTO_ID = "AUTO"
private val choosableSides = listOf(Side.PRO, Side.LEFT, Side.RIGHT)

@Composable
internal fun ControllerTypeDropdown(
    joycon: ConnectedJoycon,
    onSetType: (Side?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val detected = joycon.model.label()
    val auto = stringResource(R.string.type_auto, detected)
    val options = listOf(DropdownOption(AUTO_ID, auto)) +
        choosableSides.map { DropdownOption(it.name, it.typeLabel()) }
    OptionDropdown(
        options = options,
        selectedId = joycon.typeOverride?.name ?: AUTO_ID,
        label = joycon.typeOverride?.typeLabel() ?: auto,
        subLabel = stringResource(R.string.type_hint),
        onSelect = { id -> onSetType(if (id == AUTO_ID) null else Side.valueOf(id)) },
        modifier = modifier,
    )
}
