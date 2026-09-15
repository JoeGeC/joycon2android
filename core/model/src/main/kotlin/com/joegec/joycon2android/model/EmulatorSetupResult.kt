package com.joegec.joycon2android.model

/** Outcome of a one-shot emulator config write. */
enum class EmulatorSetupResult {
    SUCCESS,
    NO_PRIVILEGED_ACCESS,

    /**
     * The emulator was running. Emulators hold their config in memory and flush it on exit, so a
     * write while one is open is silently overwritten — verified against Eden, whose in-memory
     * bindings replaced ours on shutdown.
     */
    EMULATOR_RUNNING,
    FAILED,
}
