package com.joegec.joycon2android.output.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.ui.components.DropdownOption
import com.joegec.joycon2android.ui.components.OptionDropdown
import com.joegec.joycon2android.ui.theme.Background
import com.joegec.joycon2android.ui.theme.Dimens
import com.joegec.joycon2android.ui.theme.TextDim

/** One row per installed emulator, so Eden and Eden Nightly are each a choice of their own. */
@Composable
internal fun PlayingPicker(state: OutputCardState, onSelect: (Playing, String?) -> Unit) {
    val options = Playing.entries.flatMap { playing ->
        val targets = state.emulators[playing].orEmpty()
        if (targets.isEmpty()) listOf(DropdownOption(playing.name, playing.label()))
        else targets.map { DropdownOption(optionId(playing, it.id), it.label()) }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.elementSpacing),
    ) {
        Text(stringResource(R.string.output_playing), color = TextDim, style = MaterialTheme.typography.bodyLarge)
        Dropdown(state, options, onSelect)
    }
}

@Composable
private fun RowScope.Dropdown(state: OutputCardState, options: List<DropdownOption>, onSelect: (Playing, String?) -> Unit) {
    OptionDropdown(
        options = options,
        selectedId = state.target?.let { optionId(state.playing, it.id) } ?: state.playing.name,
        label = state.target?.label() ?: state.playing.label(),
        onSelect = { id ->
            val playing = Playing.valueOf(id.substringBefore(SEPARATOR))
            onSelect(playing, id.substringAfter(SEPARATOR, "").ifEmpty { null })
        },
        modifier = Modifier
            .weight(1f)
            .background(Background, RoundedCornerShape(Dimens.buttonCorner)),
    )
}

private fun optionId(playing: Playing, emulatorId: String) = "${playing.name}$SEPARATOR$emulatorId"

private const val SEPARATOR = '|'
