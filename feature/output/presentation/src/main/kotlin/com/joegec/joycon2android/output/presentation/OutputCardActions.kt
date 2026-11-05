package com.joegec.joycon2android.output.presentation

import com.joegec.joycon2android.output.Playing

data class OutputCardActions(
    /** [emulatorId] is null for a game with no emulator installed to pick. */
    val select: (playing: Playing, emulatorId: String?) -> Unit,
    val setUp: () -> Unit,
    val openMapping: () -> Unit,
)
