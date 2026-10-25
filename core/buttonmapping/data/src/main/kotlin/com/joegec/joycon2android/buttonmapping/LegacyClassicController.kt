package com.joegec.joycon2android.buttonmapping

/** Before the Wii controller choice, a Classic Controller was this entry among a body's bindings. */
internal const val LEGACY_CLASSIC_CONTROLLER_KEY = "CLASSIC_CONTROLLER"

internal val Map<String, String>.legacyClassicController: Boolean
    get() = this[LEGACY_CLASSIC_CONTROLLER_KEY] == "true"
