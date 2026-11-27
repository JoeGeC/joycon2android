package com.joegec.joycon2android.output.presentation

import androidx.compose.runtime.Composable
import com.joegec.joycon2android.output.EmulatorTarget
import com.joegec.joycon2android.output.Playing
import com.joegec.joycon2android.ui.components.DropdownOption

internal data class PlayingOption(val playing: Playing, val emulatorId: String?) {
    companion object {
        fun parse(id: String) = PlayingOption(
            Playing.valueOf(id.substringBefore(SEPARATOR)),
            id.substringAfter(SEPARATOR, "").ifEmpty { null },
        )
    }
}

/** One row per installed emulator, so Eden and Eden Nightly are each a choice of their own. */
@Composable
internal fun playingOptions(emulators: Map<Playing, List<EmulatorTarget>>): List<DropdownOption> =
    Playing.entries.flatMap { playing ->
        val targets = emulators[playing].orEmpty()
        if (targets.isEmpty()) listOf(DropdownOption(playing.name, playing.label()))
        else targets.map { DropdownOption(playingOptionId(playing, it), it.label()) }
    }

internal fun playingOptionId(playing: Playing, target: EmulatorTarget?): String =
    target?.let { "${playing.name}$SEPARATOR${it.id}" } ?: playing.name

private const val SEPARATOR = '|'
