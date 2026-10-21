package com.joegec.joycon2android.buttonmapping

/** Stored among a body's Wii bindings, so layouts save it: docs/dsu-motion.md#classic-controller */
const val CLASSIC_CONTROLLER_KEY = "CLASSIC_CONTROLLER"

const val CLASSIC_CONTROLLER_ON = "true"

val Map<String, String>.classicController: Boolean get() = this[CLASSIC_CONTROLLER_KEY] == CLASSIC_CONTROLLER_ON
