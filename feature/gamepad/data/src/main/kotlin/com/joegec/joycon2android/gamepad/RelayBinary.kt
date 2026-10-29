package com.joegec.joycon2android.gamepad

import android.content.Context
import com.joegec.joycon2android.gamepad.privileged.PrivilegedShell

/** A native relay packaged as `lib<name>.so`, copied where the shell user may run it. */
class RelayBinary(private val libraryName: String, private val remotePath: String) {

    @Volatile
    private var deployed = false

    fun deploy(context: Context, shell: PrivilegedShell): String {
        if (deployed) return remotePath
        val localPath = context.applicationInfo.nativeLibraryDir + "/lib$libraryName.so"
        shell.newProcess(arrayOf("sh", "-c", "cp $localPath $remotePath && chmod 755 $remotePath"))?.waitFor()
        deployed = true
        return remotePath
    }
}
