package com.joegec.joycon2android.output.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.OptionDropdown
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.Dimens

/** One row per installed emulator, so Eden and Eden Nightly are each a choice of their own. */
@Composable
internal fun PlayingPicker(state: OutputCardState, onSelect: (Playing, String?) -> Unit) {
    val options = Playing.entries.flatMap { playing ->
        val targets = state.emulators[playing].orEmpty()
        if (targets.isEmpty()) listOf(DropdownOption(playing.name, playing.label()))
        else targets.map { DropdownOption(optionId(playing, it.id), it.label()) }
    }
    OptionDropdown(
        options = options,
        selectedId = state.target?.let { optionId(state.playing, it.id) } ?: state.playing.name,
        label = state.target?.label() ?: state.playing.label(),
        onSelect = { id ->
            val playing = Playing.valueOf(id.substringBefore(SEPARATOR))
            onSelect(playing, id.substringAfter(SEPARATOR, "").ifEmpty { null })
        },
        modifier = Modifier
            .fillMaxWidth()
            .background(Background, RoundedCornerShape(Dimens.buttonCorner)),
    )
}

private fun optionId(playing: Playing, emulatorId: String) = "${playing.name}$SEPARATOR$emulatorId"

private const val SEPARATOR = '|'
