package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.output.PlayingSelection
import com.joegec.joycon2android.output.selected
import com.joegec.joycon2android.ui.components.OptionListDialog

/** What's being played on its own, for picking outside the Output card. */
@Composable
fun PlayingPickerDialog(
    selection: PlayingSelection,
    emulators: Map<Playing, List<EmulatorTarget>>,
    onSelect: (Playing, emulatorId: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val target = emulators[selection.playing].orEmpty().selected(selection.emulatorId)
    OptionListDialog(
        title = stringResource(R.string.playing_picker_title),
        options = playingOptions(emulators),
        selectedId = playingOptionId(selection.playing, target),
        dismissLabel = stringResource(R.string.playing_picker_cancel),
        onSelect = { id -> PlayingOption.parse(id).let { onSelect(it.playing, it.emulatorId) } },
        onDismiss = onDismiss,
    )
}
