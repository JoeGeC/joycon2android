package com.joegec.joycon2android.gamepad.privileged

import java.io.InputStream
import java.io.OutputStream

/**
 * A source of shell-uid processes — the one privilege the UHID relay needs (to reach
 * `/dev/uhid`). Implemented over Shizuku and over an in-app ADB/wireless-debugging
 * connection, so the relay layer is unaware of which grants the privilege.
 */
interface PrivilegedShell {
    val isReady: Boolean
    fun newProcess(argv: Array<String>): ShellProcess?

    /** Runs [script] through the device shell, hiding the per-backend argv differences. */
    fun shell(script: String): ShellProcess?
}

interface ShellProcess {
    val outputStream: OutputStream
    val inputStream: InputStream
    fun waitFor()
    fun destroy()
}
