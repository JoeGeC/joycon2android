package com.joegec.joycon2android.uhid

import android.os.ParcelFileDescriptor
import moe.shizuku.server.IShizukuService
import rikka.shizuku.Shizuku
import java.io.InputStream
import java.io.OutputStream

class ShizukuShell : PrivilegedShell {

    override val isReady: Boolean
        get() = ShizukuPermissionHandler.isShizukuAvailable && ShizukuPermissionHandler.isPermissionGranted

    override fun newProcess(argv: Array<String>): ShellProcess? {
        val service = IShizukuService.Stub.asInterface(Shizuku.getBinder()) ?: return null
        val process = service.newProcess(argv, null, null)
        return ShizukuProcess(process)
    }

    private class ShizukuProcess(private val process: moe.shizuku.server.IRemoteProcess) : ShellProcess {
        override val outputStream: OutputStream =
            ParcelFileDescriptor.AutoCloseOutputStream(process.outputStream)
        override val inputStream: InputStream =
            ParcelFileDescriptor.AutoCloseInputStream(process.inputStream)

        override fun waitFor() {
            process.waitFor()
        }

        override fun destroy() {
            try {
                process.destroy()
            } catch (_: Exception) {
            }
        }
    }
}
