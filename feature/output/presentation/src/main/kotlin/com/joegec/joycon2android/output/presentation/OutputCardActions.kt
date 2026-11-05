package com.joegec.joycon2android.output.presentation

import com.joegec.joycon2android.output.Playing

data class OutputCardActions(
    val setOn: (Boolean) -> Unit,
    val setPlaying: (Playing) -> Unit,
    val selectEmulator: (String) -> Unit,
    val setMotion: (Boolean) -> Unit,
    val setUp: () -> Unit,
    val openMapping: () -> Unit,
)
