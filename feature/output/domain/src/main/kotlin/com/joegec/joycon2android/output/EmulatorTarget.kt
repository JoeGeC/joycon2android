package com.joegec.joycon2android.output

/**
 * [name] is the emulator's own, e.g. "Dolphin (Wii)"; Dolphin is two targets of one [packageName].
 * Both are null for [OTHER], which names no app and so has nothing to set up.
 */
data class EmulatorTarget(val id: String, val packageName: String?, val name: String?, val kind: EmulatorKind) {
    val setsUp: Boolean get() = packageName != null

    companion object {
        val OTHER = EmulatorTarget("other", packageName = null, name = null, EmulatorKind.OTHER)
    }
}

/** The one [id] names, or the first listed when it names none (never chosen, or uninstalled since). */
fun List<EmulatorTarget>.selected(id: String?): EmulatorTarget? = firstOrNull { it.id == id } ?: firstOrNull()
