package com.joegec.joycon2android.output

/**
 * [name] is the emulator's own, e.g. "Dolphin (Wii)"; Dolphin is two targets of one [packageName].
 * Both are null for the [OTHERS], which name no app.
 */
data class EmulatorTarget(val id: String, val packageName: String?, val name: String?, val kind: EmulatorKind) {

    companion object {
        val OTHERS = listOf(
            EmulatorTarget("other-gamepad", packageName = null, name = null, EmulatorKind.OTHER_GAMEPAD),
            EmulatorTarget("other-motion", packageName = null, name = null, EmulatorKind.OTHER_MOTION),
        )
    }
}

/** The one [id] names, or the first listed when it names none (never chosen, or uninstalled since). */
fun List<EmulatorTarget>.selected(id: String?): EmulatorTarget? = firstOrNull { it.id == id } ?: firstOrNull()
