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

@Composable
internal fun PlayingPicker(state: OutputCardState, onSelect: (Playing, String?) -> Unit) {
    val options = playingOptions(state.emulators)
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
        selectedId = playingOptionId(state.playing, state.target),
        label = state.target?.label() ?: state.playing.label(),
        onSelect = { id -> PlayingOption.parse(id).let { onSelect(it.playing, it.emulatorId) } },
        modifier = Modifier
            .weight(1f)
            .background(Background, RoundedCornerShape(Dimens.buttonCorner)),
    )
}
