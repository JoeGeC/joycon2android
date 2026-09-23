package com.joegec.joycon2android.gamepad.privileged

import java.io.InputStream
import java.io.OutputStream

/** Shell-uid processes, for `/dev/uhid` and other apps' config files. */
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
