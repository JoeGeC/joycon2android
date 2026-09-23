package com.joegec.joycon2android.model

enum class EmulatorSetupResult {
    SUCCESS,
    NO_PRIVILEGED_ACCESS,

    /** Emulators flush their in-memory config over ours on exit (seen on Eden), so it must close first. */
    EMULATOR_RUNNING,
    FAILED,
}
