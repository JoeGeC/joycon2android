package com.joegec.joycon2android.buttonmapping

import com.joegec.joycon2android.buttonmapping.preset.SwitchProSidewaysMapping

/** Before the Pro Controller switch, every lone Joy-Con's Switch bindings were the sideways grip's. */
internal fun Map<String, String>.withLegacyGrip(console: Console, side: JoyconSide): Map<String, String> =
    if (console == Console.SWITCH_PRO && side.isLone) SwitchProSidewaysMapping.entries(side) + this else this
