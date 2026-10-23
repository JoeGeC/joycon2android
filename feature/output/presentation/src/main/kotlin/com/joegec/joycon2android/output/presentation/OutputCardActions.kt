package com.joegec.joycon2android.output.presentation

import com.joegec.joycon2android.output.DeviceOverride
import com.joegec.joycon2android.output.OutputDevice
import com.joegec.joycon2android.output.OutputMode

data class OutputCardActions(
    val toggle: (Boolean) -> Unit,
    val setMode: (OutputMode) -> Unit,
    val selectEmulator: (String) -> Unit,
    val setUp: () -> Unit,
    val openMapping: () -> Unit,
    val setOverride: (OutputDevice, DeviceOverride) -> Unit,
)
