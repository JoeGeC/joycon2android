package com.joegec.joycon2android.uhid

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.joegec.joycon2android.IUhidService
import com.joegec.joycon2android.service.ShizukuUhidService
import kotlinx.coroutines.suspendCancellableCoroutine
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object UhidServiceBridge {

    private const val TAG = "UhidServiceBridge"
    private var service: IUhidService? = null
    private var serviceConnection: ServiceConnection? = null

    val isBound: Boolean get() = service != null

    suspend fun bind(): IUhidService {
        service?.let { return it }
        return bindService()
    }

    fun getService(): IUhidService? = service

    private suspend fun bindService(): IUhidService = suspendCancellableCoroutine { cont ->
        val args = Shizuku.UserServiceArgs(
            ComponentName(
                "com.joegec.joycon2android",
                ShizukuUhidService::class.java.name,
            ),
        )
            .daemon(false)
            .processNameSuffix("uhid")
            .debuggable(false)
            .version(1)

        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val svc = IUhidService.Stub.asInterface(binder)
                service = svc
                serviceConnection = this
                Log.i(TAG, "Shizuku UHID service connected")
                if (cont.isActive) cont.resume(svc)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
                serviceConnection = null
                Log.w(TAG, "Shizuku UHID service disconnected")
                if (cont.isActive) {
                    cont.resumeWithException(RuntimeException("Shizuku service disconnected"))
                }
            }
        }

        try {
            Shizuku.bindUserService(args, connection)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind Shizuku user service", e)
            if (cont.isActive) cont.resumeWithException(e)
        }
    }

    fun unbind() {
        serviceConnection?.let { conn ->
            try {
                Shizuku.unbindUserService(
                    Shizuku.UserServiceArgs(
                        ComponentName(
                            "com.joegec.joycon2android",
                            ShizukuUhidService::class.java.name,
                        ),
                    ),
                    conn,
                    false,
                )
            } catch (_: Exception) {}
        }
        service = null
        serviceConnection = null
    }
}
