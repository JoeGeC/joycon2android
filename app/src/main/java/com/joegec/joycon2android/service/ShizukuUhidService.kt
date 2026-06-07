package com.joegec.joycon2android.service

import android.util.Log
import com.joegec.joycon2android.IUhidService
import java.io.File

class ShizukuUhidService : IUhidService.Stub() {

    private val devices = mutableMapOf<Int, UhidDevice>()

    init {
        // Log process info for debugging access issues
        try {
            val uid = android.os.Process.myUid()
            val pid = android.os.Process.myPid()
            Log.i(TAG, "Service started — UID=$uid, PID=$pid")

            val uhidFile = File("/dev/uhid")
            Log.i(TAG, "/dev/uhid exists=${uhidFile.exists()}, canRead=${uhidFile.canRead()}, canWrite=${uhidFile.canWrite()}")
        } catch (e: Throwable) {
            Log.e(TAG, "Init diagnostics failed", e)
        }
    }

    override fun createDevice(playerIndex: Int, name: String): Boolean {
        return try {
            if (devices.containsKey(playerIndex)) return true

            val device = UhidDevice(name, playerIndex)
            val success = device.create()
            if (success) {
                devices[playerIndex] = device
            }
            success
        } catch (e: Throwable) {
            Log.e(TAG, "createDevice crashed", e)
            false
        }
    }

    override fun sendReport(playerIndex: Int, report: ByteArray): Boolean {
        return try {
            devices[playerIndex]?.sendReport(report) ?: false
        } catch (e: Throwable) {
            false
        }
    }

    override fun destroyDevice(playerIndex: Int) {
        try {
            devices.remove(playerIndex)?.destroy()
        } catch (e: Throwable) {
            Log.e(TAG, "destroyDevice crashed", e)
        }
    }

    override fun destroyAll() {
        try {
            devices.values.forEach { it.destroy() }
            devices.clear()
        } catch (e: Throwable) {
            Log.e(TAG, "destroyAll crashed", e)
        }
    }

    companion object {
        private const val TAG = "ShizukuUhidService"
    }
}
