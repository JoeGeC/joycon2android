package com.joegec.joycon2android.update

sealed interface InstallProgress {

    data class Downloading(val percent: Int) : InstallProgress

    /** Android blocks sideloaded installs until the user allows this app to install others. */
    data object NeedsPermission : InstallProgress

    /** The APK is with the system installer, which owns the rest of the flow. */
    data object HandedOff : InstallProgress

    data object Failed : InstallProgress
}
