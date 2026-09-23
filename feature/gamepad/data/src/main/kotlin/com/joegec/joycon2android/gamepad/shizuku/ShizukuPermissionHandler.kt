package com.joegec.joycon2android.gamepad.shizuku

import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import rikka.shizuku.Shizuku

object ShizukuPermissionHandler {

    private const val TAG = "ShizukuPermission"
    private const val REQUEST_CODE = 1001

    private val mainHandler = Handler(Looper.getMainLooper())

    val isShizukuAvailable: Boolean
        get() = try {
            Shizuku.pingBinder()
        } catch (_: Exception) {
            false
        }

    val isPermissionGranted: Boolean
        get() = try {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) {
            false
        }

    // Must be issued on the main thread: from any other, Shizuku drops the callback and the caller hangs.
    fun requestPermission(callback: (granted: Boolean) -> Unit) {
        if (isPermissionGranted) {
            callback(true)
            return
        }

        onMainThread {
            val listener = object : Shizuku.OnRequestPermissionResultListener {
                override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                    if (requestCode == REQUEST_CODE) {
                        Shizuku.removeRequestPermissionResultListener(this)
                        val granted = grantResult == PackageManager.PERMISSION_GRANTED
                        Log.i(TAG, "Shizuku permission ${if (granted) "granted" else "denied"}")
                        callback(granted)
                    }
                }
            }

            Shizuku.addRequestPermissionResultListener(listener)
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }

    private fun onMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) block() else mainHandler.post(block)
    }
}
