package com.joegec.joycon2android.gamepad.shizuku

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku

object ShizukuPermissionHandler {

    private const val TAG = "ShizukuPermission"
    private const val REQUEST_CODE = 1001

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

    fun requestPermission(callback: (granted: Boolean) -> Unit) {
        if (isPermissionGranted) {
            callback(true)
            return
        }

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
